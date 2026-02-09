package com.qzh.backend.service.Impl;

import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.config.AliPayConfig;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.AmountOrderMapper;
import com.qzh.backend.model.dto.product.AmountOrderQueryDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.PayStatusEnum;
import com.qzh.backend.model.vo.AmountOrderDetailVO;
import com.qzh.backend.service.AmountOrderService;
import com.qzh.backend.service.PurchaseOrderService;
import com.qzh.backend.service.PurchaseReturnService;
import com.qzh.backend.service.SaleOrderService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AmountOrderServiceImpl extends ServiceImpl<AmountOrderMapper, AmountOrder> implements AmountOrderService {

    private final AppGlobalConfig  appGlobalConfig;

    private final GetLoginUserUtil getLoginUserUtil;

    private final AliPayConfig aliPayConfig;

    private static final String GATEWAY_URL = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private static final String FORMAT = "JSON";

    private static final String CHARSET = "UTF-8";

    private static final String SIGN_TYPE = "RSA2";

    @Resource
    @Lazy
    private PurchaseOrderService purchaseOrderService;

    private final PurchaseReturnService purchaseReturnService;

    @Resource
    @Lazy
    private SaleOrderService saleOrderService;

    @Resource
    @Lazy
    private SaleReturnServiceImpl saleReturnService;

    @Override
    public Page<AmountOrder> listAmountOrdersByStoreId(AmountOrderQueryDTO queryDTO) {
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size <= 0 || size > 1000,ErrorCode.PARAMS_ERROR);
        Page<AmountOrder> page = new Page<>(current, size);
        queryDTO.setStoreId(appGlobalConfig.getCurrentStoreId());
        return this.page(page, AmountOrderQueryDTO.getQueryWrapper(queryDTO));
    }

    @Override
    public void payOrder(Long id,HttpServletRequest request, HttpServletResponse response) throws IOException{
        payMoney(id,request,response);
    }

    @Override
    public void cancleOrder(Long id,HttpServletRequest request, HttpServletResponse response) throws IOException{
        payMoney(id,request,response);
    }

    private void payMoney(Long id,HttpServletRequest request, HttpServletResponse response)  throws IOException{
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        AmountOrder amountOrder = this.getById(id);
        ThrowUtils.throwIf(amountOrder == null, ErrorCode.NOT_FOUND_ERROR,"订单不存在");
        Long payerId = amountOrder.getPayerId();
        User loginUser = getLoginUserUtil.getLoginUser(request);
        if(!payerId.equals(loginUser.getId())){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"该订单不由你支付");
        }
        AlipayClient aliPayClient = new DefaultAlipayClient(GATEWAY_URL,aliPayConfig.getAppId(),aliPayConfig.getAppPrivateKey(),FORMAT,CHARSET,aliPayConfig.getAlipayPublicKey(),SIGN_TYPE);
        AlipayTradePagePayRequest req = new AlipayTradePagePayRequest();
        req.setNotifyUrl(aliPayConfig.getNotifyUrl());
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", amountOrder.getId());
        bizContent.put("total_amount", amountOrder.getAmount());
        bizContent.put("subject","orderId" + amountOrder.getOrderId());
        bizContent.put("product_code","FAST_INSTANT_TRADE_PAY");
        req.setBizContent(bizContent.toString());
//        req.setReturnUrl("http://localhost:9090/home/hello");
        String form = "";
        try{
            form = aliPayClient.pageExecute(req).getBody();
        }catch (AlipayApiException e){
            System.out.println(e.getMessage());
        }
        response.setContentType("text/html;charset=" + CHARSET);
        response.getWriter().write(form);
        response.getWriter().flush();
        response.getWriter().close();
    }

    @Override
    public AmountOrderDetailVO getAmountOrderDetail(Long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id == null || id <= 0,ErrorCode.PARAMS_ERROR);
        AmountOrder amountOrder = this.getById(id);
        ThrowUtils.throwIf(amountOrder == null, ErrorCode.NOT_FOUND_ERROR,"订单不存在");
        User loginUser = getLoginUserUtil.getLoginUser(request);
        if(!loginUser.getId().equals(amountOrder.getPayerId()) && !loginUser.getId().equals(amountOrder.getPayeeId())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR,"该订单不属于你，无法查看");
        }
        AmountOrderDetailVO amountOrderDetailVO = new AmountOrderDetailVO();
        amountOrderDetailVO.setAmount(amountOrder.getAmount());
        amountOrderDetailVO.setAmountOrderStatus(amountOrder.getStatus());
        amountOrderDetailVO.setAmountOrderId(amountOrder.getId());
        amountOrderDetailVO.setPayType(amountOrder.getPayType());
        amountOrderDetailVO.setPayerId(amountOrder.getPayerId());
        amountOrderDetailVO.setStoreId(appGlobalConfig.getCurrentStoreId());
        amountOrderDetailVO.setStoreName(appGlobalConfig.getCurrentStoreName());
        amountOrderDetailVO.setTradeNo(amountOrder.getTradeNo());
        Long orderId = amountOrder.getOrderId();
        switch (amountOrder.getType()) {
            case 0:
                PurchaseOrder purchaseOrder = purchaseOrderService.getById(orderId);
                amountOrderDetailVO.setProductId(purchaseOrder.getProductId());
                amountOrderDetailVO.setProductName(purchaseOrder.getProductName());
                amountOrderDetailVO.setProductDescription(purchaseOrder.getProductDescription());
                amountOrderDetailVO.setProductPrice(purchaseOrder.getProductPrice());
                amountOrderDetailVO.setProductQuantity(purchaseOrder.getProductQuantity());
                amountOrderDetailVO.setProductUrl(purchaseOrder.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(purchaseOrder.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            case 1:
                PurchaseReturn purchaseReturn = purchaseReturnService.getById(orderId);
                amountOrderDetailVO.setProductId(purchaseReturn.getProductId());
                amountOrderDetailVO.setProductName(purchaseReturn.getProductName());
                amountOrderDetailVO.setProductDescription(purchaseReturn.getProductDescription());
                amountOrderDetailVO.setProductPrice(purchaseReturn.getProductPrice());
                amountOrderDetailVO.setProductQuantity(purchaseReturn.getProductQuantity());
                amountOrderDetailVO.setProductUrl(purchaseReturn.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(purchaseReturn.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            case 2:
                SaleOrder saleOrder = saleOrderService.getById(orderId);
                amountOrderDetailVO.setProductId(saleOrder.getProductId());
                amountOrderDetailVO.setProductName(saleOrder.getProductName());
                amountOrderDetailVO.setProductDescription(saleOrder.getProductDescription());
                amountOrderDetailVO.setProductPrice(saleOrder.getProductPrice());
                amountOrderDetailVO.setProductQuantity(saleOrder.getProductQuantity());
                amountOrderDetailVO.setProductUrl(saleOrder.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(saleOrder.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            case 3:
                SaleReturn saleReturn = saleReturnService.getById(orderId);
                amountOrderDetailVO.setProductId(saleReturn.getProductId());
                amountOrderDetailVO.setProductName(saleReturn.getProductName());
                amountOrderDetailVO.setProductDescription(saleReturn.getProductDescription());
                amountOrderDetailVO.setProductPrice(saleReturn.getProductPrice());
                amountOrderDetailVO.setProductQuantity(saleReturn.getProductQuantity());
                amountOrderDetailVO.setProductUrl(saleReturn.getProductUrl());
                amountOrderDetailVO.setPurchaseOrderStatus(saleReturn.getStatus());
                amountOrderDetailVO.setOrderId(orderId);
                break;
            default:
                ThrowUtils.throwIf(true, ErrorCode.PARAMS_ERROR, "未知的金额单类型：" + amountOrder.getType());
        }
        return amountOrderDetailVO;
    }

    @Override
    public void notifyOrder(HttpServletRequest request) throws AlipayApiException {
        if (request.getParameter("trade_status").equals("TRADE_SUCCESS")){
            System.out.println("======支付宝异步回调======");
            Map<String,String> params = new HashMap<>();
            Map<String,String[]> requestParams = request.getParameterMap();
            for(String name : requestParams.keySet()){
                params.put(name, request.getParameter(name));
            }
            String sign = params.get("sign");
            String content = AlipaySignature.getSignCheckContentV1(params);
            boolean checkSignature = AlipaySignature.rsa256CheckContent(content,sign,aliPayConfig.getAlipayPublicKey(),"UTF-8");
            if (checkSignature){
                // 验签通过
                System.out.println("交易名称:"+ params.get("subject"));
                System.out.println("交易状态："+ params.get("trade_status"));
                System.out.println("支付宝交易凭证号："+params.get("trade_no"));
                System.out.println("商户订单号:"+ params.get("out_trade_no"));
                System.out.println("交易金额:"+params.get("total_amount"));
                System.out.println("买家在支付宝唯一id:"+params.get("buyer_id"));
                System.out.println("买家付款时间:"+ params.get("gmt_payment"));
                System.out.println("买家付款金额:"+ params.get("buyer_pay_amount"));
                String status = params.get("trade_status");
                if("TRADE_SUCCESS".equals(status)){
                    String amountId = params.get("out_trade_no");
                    AmountOrder amountOrder = this.getById(amountId);
                    amountOrder.setStatus(PayStatusEnum.PAID.getValue());
                    boolean b = this.updateById(amountOrder);
                    ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR,"订单支付状态更新失败");
                }else {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR,"订单支付失败");
                }
            }else {
                throw new BusinessException(ErrorCode.OPERATION_ERROR,"订单支付失败");
            }
        }
    }
}