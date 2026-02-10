package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.SaleOrderCreateDTO;
import com.qzh.backend.model.dto.product.SaleOrderQueryDTO;
import com.qzh.backend.model.entity.SaleOrder;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.qzh.backend.constants.Interface.SaleOrderInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.SALE_ORDER_MODULE;

@RestController
@RequestMapping("/sale/order")
@RequiredArgsConstructor
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    /**
     * 创建销售订单
     */
    @PostMapping("/create")
    @AuthCheck(interfaceName = SALE_ORDER_CREATE_POST)
    @LogInfoRecord(SystemModule = SALE_ORDER_MODULE + ":" + SALE_ORDER_CREATE_POST)
    public BaseResponse<Long> createSaleOrder(@Valid @RequestBody SaleOrderCreateDTO createDTO, HttpServletRequest request) {
        Long saleOrderId = saleOrderService.createSaleOrder(createDTO,request);
        return ResultUtils.success(saleOrderId);
    }

    /**
     * 查询我的销售订单列表
     */
    @GetMapping("/my")
    @AuthCheck(interfaceName = SALE_ORDER_MY_GET)
    public BaseResponse<Page<SaleOrder>> listMySaleOrders(SaleOrderQueryDTO queryDTO, HttpServletRequest request) {
        Page<SaleOrder> orderPage = saleOrderService.listMyOrders(queryDTO,request);
        return ResultUtils.success(orderPage);
    }

    /**
     * 根据ID查询销售订单详情
     */
    @GetMapping("{id}")
    @AuthCheck(interfaceName = SALE_ORDER_DETAIL_GET)
    public BaseResponse<SaleOrder> getSaleOrderById(@PathVariable Long id) {
        SaleOrder saleOrder = saleOrderService.getById(id);
        ThrowUtils.throwIf(saleOrder == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(saleOrder);
    }

    /**
     * 查询门店销售订单列表
     */
    @GetMapping("/store")
    @AuthCheck(interfaceName = SALE_ORDER_STORE_GET)
    public BaseResponse<Page<SaleOrder>> listStoreSaleOrders(SaleOrderQueryDTO queryDTO) {
        Page<SaleOrder> page = saleOrderService.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), SaleOrderQueryDTO.getQueryWrapper(queryDTO));
        return ResultUtils.success(page);
    }

    /**
     * 确认订单到货
     */
    @PostMapping("/confirm/{id}")
    @AuthCheck(interfaceName = SALE_ORDER_CONFIRM_POST)
    @LogInfoRecord(SystemModule = SALE_ORDER_MODULE + ":" + SALE_ORDER_CONFIRM_POST)
    public BaseResponse<Void> confirmOrderArrival(@PathVariable Long id, HttpServletRequest request) {
        saleOrderService.confirmOrderArrival(id, request);
        return ResultUtils.success(null);
    }

}