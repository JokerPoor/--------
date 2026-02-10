package com.qzh.backend.controller;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.AmountOrderQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.model.vo.AmountOrderDetailVO;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.utils.GetLoginUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * 金额单Controller
 */
@RestController
@RequestMapping("/amount/order")
@RequiredArgsConstructor
public class AmountOrderController {

    private final AmountOrderService amountOrderService;

    private final GetLoginUserUtil getLoginUserUtil;

    /**
     * 分页查询门店下的所有金额单
     */
    @GetMapping("/list")
    public BaseResponse<Page<AmountOrder>> listAmountOrders(AmountOrderQueryDTO queryDTO) {
        Page<AmountOrder> amountOrderPage = amountOrderService.listAmountOrdersByStoreId(queryDTO);
        return ResultUtils.success(amountOrderPage);
    }

    /**
     * 分页查询自己为付款人的金额单
     */
    @GetMapping("/list/payer")
    public BaseResponse<Page<AmountOrder>> listAmountOrdersByPayer(AmountOrderQueryDTO queryDTO, HttpServletRequest request) {
        User loginUser = getLoginUserUtil.getLoginUser(request);
        queryDTO.setPayerId(loginUser.getId());
        Page<AmountOrder> amountOrderPage = amountOrderService.listAmountOrdersByStoreId(queryDTO);
        return ResultUtils.success(amountOrderPage);
    }

    /**
     * 分页查询自己为收款人的金额单
     */
    @GetMapping("/list/payee")
    public BaseResponse<Page<AmountOrder>> listAmountOrdersByPayee(AmountOrderQueryDTO queryDTO, HttpServletRequest request) {
        User loginUser = getLoginUserUtil.getLoginUser(request);
        queryDTO.setPayeeId(loginUser.getId());
        Page<AmountOrder> amountOrderPage = amountOrderService.listAmountOrdersByStoreId(queryDTO);
        return ResultUtils.success(amountOrderPage);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/{id}")
    public BaseResponse<AmountOrderDetailVO> getAmountOrderById(@PathVariable Long id,HttpServletRequest request) {
        AmountOrderDetailVO amountOrderDetail = amountOrderService.getAmountOrderDetail(id, request);
        return ResultUtils.success(amountOrderDetail);
    }

    /**
     * 支付订单
     */
    @PostMapping("/payorder/{id}")
    public void payOrder(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        amountOrderService.payOrder(id,request,response);
    }

    /**
     * 支付订单
     */
    @PostMapping("/cancelorder/{id}")
    public BaseResponse<Void> cancelorder(@PathVariable Long id,HttpServletRequest request,HttpServletResponse response) throws IOException {
        amountOrderService.cancleOrder(id,request,response);
        return ResultUtils.success(null);
    }

    @PostMapping("/notify")
    public void notifyOrder(HttpServletRequest request) throws AlipayApiException {
        amountOrderService.notifyOrder(request);
    }

    /**
     * 模拟支付（一键支付）
     */
    @PostMapping("/mock-pay/{id}")
    public BaseResponse<Void> mockPay(@PathVariable Long id) {
        amountOrderService.mockPay(id);
        return ResultUtils.success(null);
    }
}