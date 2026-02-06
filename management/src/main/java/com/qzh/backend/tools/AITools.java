package com.qzh.backend.tools;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseQueryDTO;
import com.qzh.backend.model.entity.Product;
import com.qzh.backend.model.entity.SaleOrder;
import com.qzh.backend.model.entity.Warehouse;
import com.qzh.backend.model.enums.SaleOrderStatusEnum;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.InventoryService;
import com.qzh.backend.service.ProductService;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.service.WarehouseService;
import com.qzh.backend.tools.query.SaleOrderQueryDTO;
import com.qzh.backend.tools.vo.Forecast;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

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

import static com.qzh.backend.utils.ThrowUtils.throwIf;

@RequiredArgsConstructor
@Component
public class AITools {

    private final ProductService productService;

    private final SaleOrderService saleOrderService;

    private final WeatherApiClient weatherApiClient;

    private final InventoryService inventoryService;

    private final WarehouseService warehouseService;

    private final AppGlobalConfig appGlobalConfig;

    @Tool(description = "获取供应商再售商品列表、门店销售数据、未来几天天气的聚合信息，用于预估热销商品")
    public DataAggregation queryDataForSalesForecast(
            @ToolParam(description = "销售订单查询条件（可选，为空返回所有订单）", required = false) SaleOrderQueryDTO saleOrderQueryDTO
    ) throws Exception {
        List<Product> products = productService.list(new LambdaQueryWrapper<Product>().eq(Product::getStatus, 1));
        List<SaleOrder> saleOrders;
        if (saleOrderQueryDTO == null) {
            saleOrders = saleOrderService.list();
        } else {
            QueryWrapper<SaleOrder> queryWrapper = SaleOrderQueryDTO.getQueryWrapper(saleOrderQueryDTO);
            saleOrders = saleOrderService.list(queryWrapper);
        }
        List<Forecast> forecasts = weatherApiClient.getForecastsByDistrictId("110105");
        return new DataAggregation(products, saleOrders, forecasts);
    }

    @Tool(description = "分页查询库存列表，返回库存记录与实时库存数量")
    public Page<InventoryVO> queryInventories(
            @ToolParam(description = "商品ID（可选）", required = false) Long productId,
            @ToolParam(description = "商品名称（可选，模糊查询）", required = false) String productName,
            @ToolParam(description = "页码（从 1 开始，可选，默认 1）", required = false) Integer current,
            @ToolParam(description = "每页大小（1-100，可选，默认 10）", required = false) Integer size
    ) {
        int c = current == null ? 1 : current;
        int s = size == null ? 10 : size;
        throwIf(c <= 0, ErrorCode.PARAMS_ERROR);
        throwIf(s <= 0 || s > 100, ErrorCode.PARAMS_ERROR);
        InventoryQueryDTO dto = new InventoryQueryDTO();
        dto.setCurrent(c);
        dto.setSize(s);
        dto.setProductId(productId);
        dto.setProductName(productName);
        return inventoryService.listInventoriesWithQuantity(dto);
    }

    @Tool(description = "分页查询库存预警列表（实时库存数量低于预警阈值）")
    public Page<InventoryVO> queryInventoryWarnings(
            @ToolParam(description = "商品ID（可选）", required = false) Long productId,
            @ToolParam(description = "商品名称（可选，模糊查询）", required = false) String productName,
            @ToolParam(description = "页码（从 1 开始，可选，默认 1）", required = false) Integer current,
            @ToolParam(description = "每页大小（1-100，可选，默认 10）", required = false) Integer size
    ) {
        int c = current == null ? 1 : current;
        int s = size == null ? 10 : size;
        throwIf(c <= 0, ErrorCode.PARAMS_ERROR);
        throwIf(s <= 0 || s > 100, ErrorCode.PARAMS_ERROR);
        InventoryQueryDTO dto = new InventoryQueryDTO();
        dto.setCurrent(c);
        dto.setSize(s);
        dto.setProductId(productId);
        dto.setProductName(productName);
        return inventoryService.listLowStockInventoriesWithQuantity(dto);
    }

    @Tool(description = "按ID获取单条库存记录，包含实时库存数量")
    public InventoryVO getInventoryById(@ToolParam(description = "库存ID") Long inventoryId) {
        return inventoryService.getInventoryVOById(inventoryId);
    }

    @Tool(description = "分页查询仓库列表")
    public Page<Warehouse> queryWarehouses(
            @ToolParam(description = "仓库名称（可选，模糊查询）", required = false) String name,
            @ToolParam(description = "仓库地址（可选，模糊查询）", required = false) String address,
            @ToolParam(description = "页码（从 1 开始，可选，默认 1）", required = false) Integer current,
            @ToolParam(description = "每页大小（1-20，可选，默认 10）", required = false) Integer size
    ) {
        int c = current == null ? 1 : current;
        int s = size == null ? 10 : size;
        throwIf(c <= 0, ErrorCode.PARAMS_ERROR);
        throwIf(s <= 0 || s > 20, ErrorCode.PARAMS_ERROR);
        WarehouseQueryDTO dto = new WarehouseQueryDTO();
        dto.setCurrent(c);
        dto.setSize(s);
        dto.setName(name);
        dto.setAddress(address);
        return warehouseService.getWarehousePage(dto);
    }

    @Tool(description = "按ID查询仓库信息")
    public Warehouse getWarehouseById(@ToolParam(description = "仓库ID") Long warehouseId) {
        return warehouseService.getWarehouseById(warehouseId);
    }

    @Tool(description = "获取销售统计（默认最近30天），包含营业额、订单数、销量、Top商品、按日趋势")
    public SalesStats querySalesStats(
            @ToolParam(description = "起始时间（毫秒时间戳，可选）", required = false) Long startTime,
            @ToolParam(description = "截至时间（毫秒时间戳，可选）", required = false) Long endTime
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

        SalesStats resp = new SalesStats();
        resp.setRangeStart(start.getTime());
        resp.setRangeEnd(end.getTime());
        resp.setTotalRevenue(totalRevenue);
        resp.setOrderCount(orderCount);
        resp.setUnitsSold(unitsSold);
        resp.setAvgOrderValue(avgOrderValue);
        resp.setTopProducts(topProducts);
        resp.setDailyTrend(dailyTrend);
        return resp;
    }

    @Data
    @AllArgsConstructor
    public static class DataAggregation {
        private List<Product> products;
        private List<SaleOrder> saleOrders;
        private List<Forecast> forecasts;
    }

    @Data
    public static class SalesStats {
        private Long rangeStart;
        private Long rangeEnd;
        private BigDecimal totalRevenue;
        private Long orderCount;
        private Long unitsSold;
        private BigDecimal avgOrderValue;
        private List<TopProduct> topProducts;
        private List<DailyTrend> dailyTrend;
    }

    @Data
    public static class TopProduct {
        private Long productId;
        private String productName;
        private BigDecimal revenue;
        private Long unitsSold;
    }

    @Data
    public static class DailyTrend {
        private String date;
        private BigDecimal revenue;
        private Long unitsSold;
    }
}

