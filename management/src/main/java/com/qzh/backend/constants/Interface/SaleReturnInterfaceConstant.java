package com.qzh.backend.constants.Interface;

public interface SaleReturnInterfaceConstant {

    // 创建销售退货单（POST /sale/return/create）
    String SALE_RETURN_CREATE_POST = "POST:/sale/return/create";

    // 查询我的销售退货单列表（GET /sale/return/my）
    String SALE_RETURN_MY_GET = "GET:/sale/return/my";

    // 查询门店销售退货单列表（GET /sale/return/store）
    String SALE_RETURN_STORE_GET = "GET:/sale/return/store";

    // 根据ID查询销售退货单详情（GET /sale/return/{id}）
    String SALE_RETURN_DETAIL_GET = "GET:/sale/return/:id";
}