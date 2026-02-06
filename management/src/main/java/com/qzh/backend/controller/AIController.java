package com.qzh.backend.controller;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.entity.SaleOrder;
import com.qzh.backend.model.enums.SaleOrderStatusEnum;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.InventoryService;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.tools.AITools;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class AIController {

    private final ChatClient managerChatClient;

    private final AITools aiTools;

    private final InventoryService inventoryService;

    private final SaleOrderService saleOrderService;

    private final AppGlobalConfig appGlobalConfig;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt) {
        return managerChatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    @PostMapping("/chat/text")
    public BaseResponse<String> chatText(@RequestBody @Valid ChatTextRequest request) {
        String content = managerChatClient.prompt()
                .user(request.getPrompt())
                .call()
                .content();
        return ResultUtils.success(content);
    }

    @PostMapping("/forecast/hot-products")
    public BaseResponse<String> forecastHotProducts(@RequestBody(required = false) com.qzh.backend.tools.query.SaleOrderQueryDTO saleOrderQueryDTO) {
        AITools.DataAggregation dataAggregation;
        try {
            dataAggregation = aiTools.queryDataForSalesForecast(saleOrderQueryDTO);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

        String prompt = """
                请基于我提供的真实数据，预测未来 7 天门店可能热销的商品，并给出建议补货数量区间。
                约束：
                1) 不得编造任何数据，只能基于我提供的数据推断；
                2) 输出必须是严格 JSON 数组，且只输出 JSON，不要输出其他文字；
                3) 每一项包含字段：productId, productName, reason, suggestedPurchaseMin, suggestedPurchaseMax, confidence。

                数据(JSON)：
                %s
                """.formatted(JSON.toJSONString(dataAggregation));

        String content = managerChatClient.prompt()
                .user(prompt)
                .call()
                .content();
        return ResultUtils.success(content);
    }

    @GetMapping("/inventory/warnings")
    public BaseResponse<com.baomidou.mybatisplus.extension.plugins.pagination.Page<InventoryVO>> inventoryWarnings(@Valid InventoryQueryDTO queryDTO) {
        return ResultUtils.success(inventoryService.listLowStockInventoriesWithQuantity(queryDTO));
    }

    @GetMapping("/stats/sales")
    public BaseResponse<SalesStatsResponse> salesStats(
            @RequestParam(value = "startTime", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) Long endTime
    ) {
        long now = System.currentTimeMillis();
        long defaultStart = now - 30L * 24 * 60 * 60 * 1000;
        Date start = new Date(startTime == null ? defaultStart : startTime);
        Date end = new Date(endTime == null ? now : endTime);

        Long storeId = appGlobalConfig.getCurrentStoreId();
        List<SaleOrder> saleOrders = saleOrderService.list(
                Wrappers.lambdaQuery(SaleOrder.class)
                        .eq(storeId != null, SaleOrder::getStoreId, storeId)
                        .eq(SaleOrder::getStatus, SaleOrderStatusEnum.OVERY.getValue())
                        .ge(SaleOrder::getCreateTime, start)
                        .lt(SaleOrder::getCreateTime, end)
        );

        BigDecimal totalRevenue = saleOrders.stream()
                .map(SaleOrder::getTotalAmount)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long orderCount = saleOrders.size();
        long unitsSold = saleOrders.stream()
                .map(SaleOrder::getProductQuantity)
                .filter(v -> v != null)
                .mapToLong(Integer::longValue)
                .sum();

        BigDecimal avgOrderValue = orderCount == 0
                ? BigDecimal.ZERO
                : totalRevenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);

        Map<Long, List<SaleOrder>> byProductId = saleOrders.stream()
                .filter(o -> o.getProductId() != null)
                .collect(Collectors.groupingBy(SaleOrder::getProductId));

        List<TopProduct> topProducts = byProductId.values().stream()
                .map(list -> {
                    SaleOrder any = list.get(0);
                    BigDecimal revenue = list.stream()
                            .map(SaleOrder::getTotalAmount)
                            .filter(v -> v != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long units = list.stream()
                            .map(SaleOrder::getProductQuantity)
                            .filter(v -> v != null)
                            .mapToLong(Integer::longValue)
                            .sum();
                    TopProduct tp = new TopProduct();
                    tp.setProductId(any.getProductId());
                    tp.setProductName(any.getProductName());
                    tp.setRevenue(revenue);
                    tp.setUnitsSold(units);
                    return tp;
                })
                .sorted(Comparator.comparing(TopProduct::getRevenue, Comparator.nullsFirst(Comparator.naturalOrder())).reversed())
                .limit(10)
                .toList();

        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        Map<String, List<SaleOrder>> byDay = saleOrders.stream()
                .collect(Collectors.groupingBy(o -> {
                    Date ct = o.getCreateTime();
                    Instant instant = ct == null ? Instant.ofEpochMilli(0) : ct.toInstant();
                    LocalDate ld = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                    return dateFmt.format(ld);
                }));

        List<DailyTrend> dailyTrend = byDay.entrySet().stream()
                .map(e -> {
                    BigDecimal revenue = e.getValue().stream()
                            .map(SaleOrder::getTotalAmount)
                            .filter(v -> v != null)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    long units = e.getValue().stream()
                            .map(SaleOrder::getProductQuantity)
                            .filter(v -> v != null)
                            .mapToLong(Integer::longValue)
                            .sum();
                    DailyTrend t = new DailyTrend();
                    t.setDate(e.getKey());
                    t.setRevenue(revenue);
                    t.setUnitsSold(units);
                    return t;
                })
                .sorted(Comparator.comparing(DailyTrend::getDate))
                .toList();

        SalesStatsResponse resp = new SalesStatsResponse();
        resp.setRangeStart(start.getTime());
        resp.setRangeEnd(end.getTime());
        resp.setTotalRevenue(totalRevenue);
        resp.setOrderCount(orderCount);
        resp.setUnitsSold(unitsSold);
        resp.setAvgOrderValue(avgOrderValue);
        resp.setTopProducts(topProducts);
        resp.setDailyTrend(dailyTrend);
        return ResultUtils.success(resp);
    }

    @lombok.Data
    public static class ChatTextRequest {
        private String prompt;
    }

    @lombok.Data
    public static class SalesStatsResponse {
        private Long rangeStart;
        private Long rangeEnd;
        private BigDecimal totalRevenue;
        private Long orderCount;
        private Long unitsSold;
        private BigDecimal avgOrderValue;
        private List<TopProduct> topProducts;
        private List<DailyTrend> dailyTrend;
    }

    @lombok.Data
    public static class TopProduct {
        private Long productId;
        private String productName;
        private BigDecimal revenue;
        private Long unitsSold;
    }

    @lombok.Data
    public static class DailyTrend {
        private String date;
        private BigDecimal revenue;
        private Long unitsSold;
    }
}
