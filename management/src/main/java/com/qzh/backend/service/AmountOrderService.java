package com.qzh.backend.service;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qzh.backend.model.dto.product.AmountOrderQueryDTO;
import com.qzh.backend.model.entity.AmountOrder;
import com.qzh.backend.model.vo.AmountOrderDetailVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface AmountOrderService extends IService<AmountOrder> {

    /**
     * 根据门店ID分页查询金额单
     */
    Page<AmountOrder> listAmountOrdersByStoreId(AmountOrderQueryDTO queryDTO);

    void payOrder(Long id,HttpServletRequest request, HttpServletResponse response) throws IOException;

    void cancleOrder(Long id,HttpServletRequest request, HttpServletResponse response) throws IOException;

    AmountOrderDetailVO getAmountOrderDetail(Long id,HttpServletRequest request);

    Page<AmountOrder> listPermittedAmountOrders(AmountOrderQueryDTO queryDTO, HttpServletRequest request);

    void notifyOrder(HttpServletRequest request) throws AlipayApiException;

    void mockPay(Long id);
}