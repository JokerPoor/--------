package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.model.dto.product.SaleReturnCreateDTO;
import com.qzh.backend.model.dto.product.SaleReturnQueryDTO;
import com.qzh.backend.model.entity.SaleReturn;
import com.qzh.backend.service.SaleReturnService;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.qzh.backend.constants.Interface.SaleReturnInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.SALE_RETURN_MODULE;

@RestController
@RequestMapping("/sale/return")
@RequiredArgsConstructor
public class SaleReturnController {

    private final SaleReturnService saleReturnService;

    /**
     * 创建销售退货单
     */
    @PostMapping("/create")
    @AuthCheck(interfaceName = SALE_RETURN_CREATE_POST)
    @LogInfoRecord(SystemModule = SALE_RETURN_MODULE + ":" + SALE_RETURN_CREATE_POST)
    public BaseResponse<Long> createSaleReturn(@RequestBody SaleReturnCreateDTO dto, HttpServletRequest request) {
        Long returnOrderId = saleReturnService.createSaleReturn(dto, request);
        return ResultUtils.success(returnOrderId);
    }

    /**
     * 查询我的销售退货单列表
     */
    @GetMapping("/my")
    @AuthCheck(interfaceName = SALE_RETURN_MY_GET)
    public BaseResponse<Page<SaleReturn>> listMySaleReturns(SaleReturnQueryDTO queryDTO,HttpServletRequest request) {
        Page<SaleReturn> returnPage = saleReturnService.listMySaleReturns(queryDTO,request);
        return ResultUtils.success(returnPage);
    }

    /**
     * 查询门店销售退货单列表
     */
    @GetMapping("/store")
    @AuthCheck(interfaceName = SALE_RETURN_STORE_GET)
    public BaseResponse<Page<SaleReturn>> listStoreSaleOrders(SaleReturnQueryDTO queryDTO) {
        Page<SaleReturn> page = saleReturnService.page(new Page<>(queryDTO.getCurrent(), queryDTO.getSize()), SaleReturnQueryDTO.getQueryWrapper(queryDTO));
        return ResultUtils.success(page);
    }

    /**
     * 根据ID查询销售退货单详情
     */
    @GetMapping("{id}")
    @AuthCheck(interfaceName = SALE_RETURN_DETAIL_GET)
    public BaseResponse<SaleReturn> getSaleOrderById(@PathVariable Long id) {
        SaleReturn saleReturn = saleReturnService.getById(id);
        ThrowUtils.throwIf(saleReturn == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(saleReturn);
    }

}