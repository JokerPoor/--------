package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.PurchaseOrderCreateDTO;
import com.qzh.backend.model.dto.product.PurchaseOrderQueryDTO;
import com.qzh.backend.model.vo.PurchaseOrderListVO;
import com.qzh.backend.service.PurchaseOrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.qzh.backend.constants.Interface.PurchaseOrderInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.PURCHASE_ORDER_MODULE;

@RestController
@RequestMapping("/purchase/order")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    /**
     * 创建采购订单
     */
    @PostMapping
    @LogInfoRecord(SystemModule = PURCHASE_ORDER_MODULE + ":" + PURCHASE_ORDER_CREATE_POST)
    public BaseResponse<Long> createPurchaseOrder(@Valid @RequestBody PurchaseOrderCreateDTO createDTO, HttpServletRequest request) {
        Long purchaseOrderId = purchaseOrderService.createPurchaseOrder(createDTO,request);
        return ResultUtils.success(purchaseOrderId);
    }

    /**
     * 查询采购订单
     */
    @GetMapping("/list")
    public BaseResponse<Page<PurchaseOrderListVO>> listPurchaseOrders(PurchaseOrderQueryDTO queryDTO) {
        Page<PurchaseOrderListVO> orderVOPage = purchaseOrderService.listPurchaseOrdersWithAmount(queryDTO);
        return ResultUtils.success(orderVOPage);
    }

    /**
     * 供应商查看收到的采购订单
     */
    @GetMapping("/supplier/list")
    public BaseResponse<Page<PurchaseOrderListVO>> listSupplierOrders(PurchaseOrderQueryDTO queryDTO, HttpServletRequest request) {
        Page<PurchaseOrderListVO> purchaseOrderPage = purchaseOrderService.listSupplierOrders(queryDTO, request);
        return ResultUtils.success(purchaseOrderPage);
    }

    /**
     * 根据采购订单ID查询
     */
    @GetMapping("/{id}")
    public BaseResponse<PurchaseOrderListVO> getPurchaseOrderById(@PathVariable Long id) {
        PurchaseOrderListVO orderVO = purchaseOrderService.getPurchaseOrderById(id);
        return ResultUtils.success(orderVO);
    }

    /**
     * 供应商设置订单为已发货
     */
    @PostMapping("/ship/{id}")
    @LogInfoRecord(SystemModule = PURCHASE_ORDER_MODULE + ":" + PURCHASE_ORDER_SHIP_POST)
    public BaseResponse<Void> shipPurchaseOrder(@PathVariable("id") Long orderId,HttpServletRequest request) {
        purchaseOrderService.shipPurchaseOrder(orderId,request);
        return ResultUtils.success(null);
    }
}