package com.qzh.backend.constants.Interface;

public interface PurchaseOrderInterfaceConstant {

    // 创建采购订单（POST /purchase/order）
    String PURCHASE_ORDER_CREATE_POST = "POST:/purchase/order";

    // 查询采购订单列表（GET /purchase/order/list）
    String PURCHASE_ORDER_LIST_GET = "GET:/purchase/order/list";

    // 根据ID查询采购订单详情（GET /purchase/order/{id}）
    String PURCHASE_ORDER_DETAIL_GET = "GET:/purchase/order/:id";

    // 供应商设置订单为已发货（POST /purchase/order/ship/{id}）
    String PURCHASE_ORDER_SHIP_POST = "POST:/purchase/order/ship/:id";
}