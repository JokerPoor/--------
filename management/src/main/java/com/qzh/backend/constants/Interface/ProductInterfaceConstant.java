package com.qzh.backend.constants.Interface;

public interface ProductInterfaceConstant {

    // 新增商品（POST /product）
    String PRODUCT_ADD_POST = "POST:/product";

    // 更新商品信息（PUT /product）
    String PRODUCT_UPDATE_PUT = "PUT:/product";

    // 删除商品（DELETE /product/{id}）
    String PRODUCT_DELETE_DELETE = "DELETE:/product/:id";

    // 根据ID查询商品详情（GET /product/{id}）
    String PRODUCT_DETAIL_GET = "GET:/product/:id";

    // 分页查询商品列表（GET /product/list）
    String PRODUCT_LIST_GET = "GET:/product/list";

    // 分页查询自有商品列表（GET /product/list/own）
    String PRODUCT_LIST_OWN_GET = "GET:/product/list/own";
}