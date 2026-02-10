package com.qzh.backend.controller;

import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.PurchaseReturnCreateDTO;
import com.qzh.backend.model.entity.PurchaseReturn;
import com.qzh.backend.service.PurchaseReturnService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.model.dto.product.PurchaseReturnQueryDTO;

/**
 * 采退订单Controller
 */
@RestController
@RequestMapping("/purchase/return")
@RequiredArgsConstructor
public class PurchaseReturnController {

    private final PurchaseReturnService purchaseReturnService;

    @GetMapping("/list")
    public BaseResponse<Page<PurchaseReturn>> listPurchaseReturns(PurchaseReturnQueryDTO queryDTO, HttpServletRequest request) {
        Page<PurchaseReturn> returnPage = purchaseReturnService.listPurchaseReturns(queryDTO, request);
        return ResultUtils.success(returnPage);
    }

    @PostMapping("/create")
    public BaseResponse<Long> createPurchaseReturn(@Valid @RequestBody PurchaseReturnCreateDTO createDTO, HttpServletRequest request) {
        Long returnOrderId = purchaseReturnService.createPurchaseReturn(createDTO,request);
        return ResultUtils.success(returnOrderId);
    }

    /**
     * 确认采退（校验金额支付状态+生成库存明细）
     * @param returnId 采退订单ID
     * @return 操作结果
     */
    @PostMapping("/confirm/{returnId}")
    public BaseResponse<Void> confirmPurchaseReturn(@PathVariable("returnId") Long returnId, HttpServletRequest request) {
        purchaseReturnService.confirmPurchaseReturn(returnId, request);
        return ResultUtils.success(null);
    }
}