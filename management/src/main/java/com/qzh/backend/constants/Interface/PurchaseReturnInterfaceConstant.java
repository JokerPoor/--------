package com.qzh.backend.constants.Interface;

public interface PurchaseReturnInterfaceConstant {

    // 查询采退订单列表（GET /purchase/return/list）
    String PURCHASE_RETURN_LIST_GET = "GET:/purchase/return/list";

    // 创建采退订单（POST /purchase/return）
    String PURCHASE_RETURN_CREATE_POST = "POST:/purchase/return";

    // 确认采退订单（POST /purchase/return/confirm/{returnId}）
    String PURCHASE_RETURN_CONFIRM_POST = "POST:/purchase/return/confirm/:returnId";
}