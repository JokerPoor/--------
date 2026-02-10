package com.qzh.backend.constants;

/**
 * 前端按钮/页面操作权限常量
 */
public interface ButtonPermissionConstant {

    // 基础角色与权限
    String ROLE_MANAGE = "角色管理";
    String PERM_ASSIGN = "权限分配";

    // 用户管理
    String USER_ADD = "user:add";
    String USER_EDIT = "user:edit";
    String USER_DELETE = "user:delete";
    String USER_RESET_PASSWORD = "user:reset-password";

    // 角色管理
    String ROLE_ADD = "role:add";
    String ROLE_EDIT = "role:edit";
    String ROLE_DELETE = "role:delete";

    // 商品管理
    String PRODUCT_ADD = "product:add";
    String PRODUCT_EDIT = "product:edit";
    String PRODUCT_DELETE = "product:delete";
    String PRODUCT_UPDATE = "product:update";

    // 仓库管理
    String WAREHOUSE_ADD = "warehouse:add";
    String WAREHOUSE_EDIT = "warehouse:edit";
    String WAREHOUSE_DELETE = "warehouse:delete";

    // 库存管理
    String INVENTORY_UPDATE = "inventory:update";

    // 采购管理
    String PURCHASE_ORDER_ADD = "purchase:order:add";
    String PURCHASE_ORDER_SHIP = "purchase:order:ship";
    String PURCHASE_ORDER_STOCK_IN = "purchase:order:stock-in";

    // 销售管理
    String SALE_ORDER_CREATE = "sale:order:create";
    String SALE_ORDER_CONFIRM = "sale:order:confirm";
    String INVENTORY_SALE_ORDER_SHIP = "inventory:sale-order:ship";
    String SALE_RETURN_ADD = "sale:return:add";

}
