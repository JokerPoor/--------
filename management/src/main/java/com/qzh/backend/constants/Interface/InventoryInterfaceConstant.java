package com.qzh.backend.constants.Interface;

public interface InventoryInterfaceConstant {

    // 分页查询入库信息（GET /inventory/list）
    String INVENTORY_LIST_GET = "GET:/inventory/list";

    // 根据库存ID获取库存信息（GET /inventory/{id}）
    String INVENTORY_DETAIL_GET = "GET:/inventory/:id";

    // 根据库存ID编辑库存中商品信息（POST /inventory/update）
    String INVENTORY_UPDATE_POST = "POST:/inventory/update";

    // 商品入库（POST /inventory/stock-in）
    String INVENTORY_STOCK_IN_POST = "POST:/inventory/stock-in";

    // 销售订单出库（POST /inventory/sale-order）
    String INVENTORY_SALE_ORDER_POST = "POST:/inventory/sale-order";

    // 确认销售退货入库（POST /inventory/sale-return/confirm）
    String INVENTORY_SALE_RETURN_CONFIRM_POST = "POST:/inventory/sale-return/confirm";
}