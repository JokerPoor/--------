package com.qzh.backend.constants.Interface;

public interface InventoryDetailInterfaceConstant {

    // 分页查询库存明细（GET /api/inventory/detail/list）
    String INVENTORY_DETAIL_LIST_GET = "GET:/api/inventory/detail/list";

    // 根据ID查询库存明细详情（GET /api/inventory/detail/{id}）
    String INVENTORY_DETAIL_DETAIL_GET = "GET:/api/inventory/detail/:id";
}