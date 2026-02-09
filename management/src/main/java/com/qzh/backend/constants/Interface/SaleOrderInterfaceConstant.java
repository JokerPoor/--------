package com.qzh.backend.constants.Interface;

public interface SaleOrderInterfaceConstant {

    // 创建销售订单（POST /sale/order/create）
    String SALE_ORDER_CREATE_POST = "POST:/sale/order/create";

    // 查询我的销售订单列表（GET /sale/order/my）
    String SALE_ORDER_MY_GET = "GET:/sale/order/my";

    // 根据ID查询销售订单详情（GET /sale/order/{id}）
    String SALE_ORDER_DETAIL_GET = "GET:/sale/order/:id";

    // 查询门店销售订单列表（GET /sale/order/store）
    String SALE_ORDER_STORE_GET = "GET:/sale/order/store";

    // 确认订单到货（POST /sale/order/confirm/{id}）
    String SALE_ORDER_CONFIRM_POST = "POST:/sale/order/confirm/:id";
}