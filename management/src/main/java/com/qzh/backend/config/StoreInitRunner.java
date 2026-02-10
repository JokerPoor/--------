package com.qzh.backend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.config.StoreInitConfig;
import com.qzh.backend.constants.ButtonPermissionConstant;
import com.qzh.backend.constants.Interface.*;
import com.qzh.backend.constants.RoleNameConstant;
import com.qzh.backend.model.entity.PageInfo;
import com.qzh.backend.model.entity.Permission;
import com.qzh.backend.model.entity.Role;
import com.qzh.backend.model.entity.RoleRelatedPage;
import com.qzh.backend.model.entity.RoleRelatedPermission;
import com.qzh.backend.model.entity.Store;
import com.qzh.backend.service.RoleService;
import com.qzh.backend.service.PageService;
import com.qzh.backend.service.PermissionService;
import com.qzh.backend.service.RoleRelatedPageService;
import com.qzh.backend.service.RoleRelatedPermissionService;
import com.qzh.backend.service.StoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 单门店初始化执行器：确保系统中只有一条门店信息
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreInitRunner implements ApplicationRunner {

    private final StoreInitConfig storeInitConfig;

    private final StoreService storeService;

    private final RoleService roleService;

    private final PageService pageService;

    private final RoleRelatedPageService roleRelatedPageService;

    private final PermissionService permissionService;

    private final RoleRelatedPermissionService roleRelatedPermissionService;

    private final AppGlobalConfig appGlobalConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("===== 开始执行单门店初始化任务 =====");
        Store existingStore = storeService.getOne(new QueryWrapper<>());
        Store configStore = storeInitConfig.toStore();
        if (existingStore == null) {
            storeService.save(configStore);
        } else {
            configStore.setId(existingStore.getId());
            storeService.updateById(configStore);
        }
        initDefaultRoles(configStore.getManagerId());
        initAdminAccess(configStore.getManagerId());
        appGlobalConfig.setCurrentStoreId(configStore.getId());
        appGlobalConfig.setCurrentStoreName(configStore.getStoreName());
        appGlobalConfig.setManagerId(configStore.getManagerId());
        log.info("===== 单门店初始化任务执行完成 =====");
        log.info("门店ID：{}，门店名：{}", appGlobalConfig.getCurrentStoreId(),appGlobalConfig.getCurrentStoreName());
    }

    private void initDefaultRoles(Long createBy) {
        List<String> roleNames = List.of(
            RoleNameConstant.ADMIN, 
            RoleNameConstant.STORE_ADMIN, 
            RoleNameConstant.SUPPLIER, 
            RoleNameConstant.CUSTOMER
        );
        for (String roleName : roleNames) {
            boolean exists = roleService.count(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, roleName)) > 0;
            if (exists) {
                continue;
            }
            Role role = new Role();
            role.setRoleName(roleName);
            role.setDescription(roleName);
            role.setCreateBy(createBy);
            roleService.save(role);
        }
    }

    private record PageSeed(String name, String path, String component, int orderNum, int visible, String parentPath) {
        public PageSeed(String name, String path, String component, int orderNum, int visible) {
            this(name, path, component, orderNum, visible, null);
        }
    }

    private void initAdminAccess(Long createBy) {
        // 1. 获取管理员角色
        Role adminRole = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, RoleNameConstant.ADMIN));
        Role storeAdminRole = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, RoleNameConstant.STORE_ADMIN));
        
        if (adminRole == null) {
            return;
        }

        // 2. 定义页面列表（包含首页）
        List<PageSeed> seeds = List.of(
                new PageSeed("系统首页", "/dashboard", "pages/dashboard/DashboardPage", 100, 1),

                // 系统管理
                new PageSeed("系统管理", "/system", null, 90, 1),
                new PageSeed("用户管理", "/users", "pages/users/UsersPage", 95, 1, "/system"),
                new PageSeed("角色管理", "/roles", "pages/roles/RolesPage", 90, 1, "/system"),
                new PageSeed("权限管理", "/permissions", "pages/permissions/PermissionsPage", 80, 1, "/system"),
                new PageSeed("页面管理", "/pages", "pages/pages/PagesPage", 70, 1, "/system"),
                new PageSeed("操作日志", "/logs", "pages/logs/OperationLogsPage", 40, 1, "/system"),
                new PageSeed("门店设置", "/store", "pages/store/StorePage", 30, 1, "/system"),
                new PageSeed("供应商管理", "/supplier", "pages/users/SupplierPage", 25, 1, "/system"),
                new PageSeed("客户管理", "/customer", "pages/users/CustomerPage", 20, 1, "/system"),

                // 商品库存
                new PageSeed("商品库存", "/goods", null, 80, 1),
                new PageSeed("商品管理", "/products", "pages/products/ProductsPage", 60, 1, "/goods"),
                new PageSeed("仓库管理", "/warehouses", "pages/warehouses/WarehousesPage", 50, 1, "/goods"),
                new PageSeed("库存管理", "/inventory", "pages/inventory/InventoryPage", 25, 1, "/goods"),

                // 采购管理
                new PageSeed("采购管理", "/purchase", null, 70, 1),
                new PageSeed("采购订单", "/purchase/order", "pages/purchase/PurchaseOrderPage", 20, 1, "/purchase"),
                new PageSeed("采购退货", "/purchase/return", "pages/purchase/PurchaseReturnPage", 15, 1, "/purchase"),

                // 销售管理
                new PageSeed("销售管理", "/sale", null, 60, 1),
                new PageSeed("销售订单", "/sale/order", "pages/sale/SaleOrderPage", 10, 1, "/sale"),
                new PageSeed("销售退货", "/sale/return", "pages/sale/SaleReturnPage", 5, 1, "/sale"),
                
                // 资金管理
                new PageSeed("资金管理", "/amount", null, 65, 1),
                new PageSeed("金额订单", "/amount/orders", "pages/amount/AmountOrderPage", 100, 1, "/amount"),

                // 供应商端
                new PageSeed("我的商品", "/supplier/products", "pages/supplier/MyProductsPage", 100, 1),
                new PageSeed("采购订单", "/supplier/orders", "pages/supplier/SupplierOrderPage", 90, 1),
                new PageSeed("采购退货", "/supplier/returns", "pages/purchase/PurchaseReturnPage", 80, 1),

                // 客户端
                new PageSeed("在线商城", "/customer/shopping", "pages/customer/ShoppingPage", 100, 1),

                // AI 智能决策
                new PageSeed("智能决策", "/ai", null, 55, 1),
                new PageSeed("智能问答", "/ai/chat", "pages/ai/AIChatPage", 100, 1, "/ai"),
                new PageSeed("库存预警", "/ai/inventory-warning", "pages/ai/InventoryWarningPage", 90, 1, "/ai"),
                new PageSeed("销量预测", "/ai/sales-forecast", "pages/ai/SalesForecastPage", 80, 1, "/ai"),
                new PageSeed("销售统计", "/ai/sales-stats", "pages/ai/SalesStatsPage", 70, 1, "/ai")
        );

        // 3. 批量创建/更新页面
        // 先确保所有页面记录存在
        Map<String, PageInfo> existingPagesByPath = pageService.list(
                new LambdaQueryWrapper<PageInfo>()
                        .in(PageInfo::getPath, seeds.stream().map(PageSeed::path).toList())
        ).stream().collect(Collectors.toMap(PageInfo::getPath, p -> p, (a, b) -> a));

        for (PageSeed seed : seeds) {
            PageInfo pageInfo = existingPagesByPath.get(seed.path());
            if (pageInfo == null) {
                pageInfo = new PageInfo();
                pageInfo.setParentId(0L); // 默认无父级
                pageInfo.setName(seed.name());
                pageInfo.setPath(seed.path());
                pageInfo.setComponent(seed.component());
                pageInfo.setOrderNum(seed.orderNum());
                pageInfo.setVisible(seed.visible());
                pageInfo.setCreateBy(createBy);
                pageService.save(pageInfo);
                existingPagesByPath.put(seed.path(), pageInfo); // Add to map for subsequent lookup
            } else {
                // 如果已存在，更新基本信息（确保数据库与代码同步）
                pageInfo.setName(seed.name());
                pageInfo.setComponent(seed.component());
                pageInfo.setPath(seed.path());
                pageInfo.setOrderNum(seed.orderNum());
                pageInfo.setVisible(seed.visible());
                pageService.updateById(pageInfo);
            }
        }

        // 4. 更新父子关系
        // 重新获取所有相关页面以确保 ID 存在
        Map<String, PageInfo> finalPagesByPath = pageService.list(
                new LambdaQueryWrapper<PageInfo>()
                        .in(PageInfo::getPath, seeds.stream().map(PageSeed::path).toList())
        ).stream().collect(Collectors.toMap(PageInfo::getPath, p -> p, (a, b) -> a));

        for (PageSeed seed : seeds) {
            if (seed.parentPath() != null) {
                PageInfo parent = finalPagesByPath.get(seed.parentPath());
                PageInfo child = finalPagesByPath.get(seed.path());
                if (parent != null && child != null && !parent.getId().equals(child.getParentId())) {
                    child.setParentId(parent.getId());
                    pageService.updateById(child);
                }
            }
        }

        // 5. 给超级管理员分配所有页面
        List<PageInfo> allPages = pageService.list(
                new LambdaQueryWrapper<PageInfo>()
                        .in(PageInfo::getPath, seeds.stream().map(PageSeed::path).toList())
        );
        assignPagesToRole(adminRole, allPages, createBy);

        // 5.1 给门店管理员分配业务页面
        if (storeAdminRole != null) {
            List<String> storeAdminPagePaths = List.of(
                "/dashboard",
                "/system", "/supplier", "/customer", // 供应商和客户管理
                "/goods", "/products", "/warehouses", "/inventory", // 商品库存
                "/purchase", "/purchase/order", "/purchase/return", // 采购
                "/sale", "/sale/order", "/sale/return", // 销售
                "/amount", "/amount/orders", // 资金管理
                "/ai", "/ai/chat", "/ai/inventory-warning", "/ai/sales-forecast", "/ai/sales-stats" // AI 智能决策
            );
            List<PageInfo> storeAdminPages = allPages.stream()
                .filter(p -> storeAdminPagePaths.contains(p.getPath()))
                .collect(Collectors.toList());
            assignPagesToRole(storeAdminRole, storeAdminPages, createBy);
        }

        // 5.2 给供应商分配业务页面
        Role supplierRole = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, RoleNameConstant.SUPPLIER));
        if (supplierRole != null) {
            List<String> supplierPagePaths = List.of(
                "/dashboard",
                "/supplier/products", "/supplier/orders", "/supplier/returns",
                "/amount", "/amount/orders"
            );
            List<PageInfo> supplierPages = allPages.stream()
                .filter(p -> supplierPagePaths.contains(p.getPath()))
                .collect(Collectors.toList());
            assignPagesToRole(supplierRole, supplierPages, createBy);
        }

        // 5.3 给其他角色（客户）分配业务页面
        List<String> customerPagePaths = List.of(
            "/dashboard", "/customer/shopping",
            "/amount", "/amount/orders"
        );
        
        List<String> otherRoles = List.of(RoleNameConstant.CUSTOMER);
        for (String rName : otherRoles) {
            Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getRoleName, rName));
            if (role != null) {
                List<PageInfo> customerPages = allPages.stream()
                    .filter(p -> customerPagePaths.contains(p.getPath()))
                    .collect(Collectors.toList());
                assignPagesToRole(role, customerPages, createBy);
            }
        }

        // 6. 初始化权限（保持原有逻辑）
        List<String> basePermNames = List.of(
                // 基础角色与权限
                ButtonPermissionConstant.ROLE_MANAGE, ButtonPermissionConstant.PERM_ASSIGN,

                // 按钮权限 - 用户管理
                ButtonPermissionConstant.USER_ADD, ButtonPermissionConstant.USER_EDIT, ButtonPermissionConstant.USER_DELETE, ButtonPermissionConstant.USER_RESET_PASSWORD,

                // 按钮权限 - 角色管理
                ButtonPermissionConstant.ROLE_ADD, ButtonPermissionConstant.ROLE_EDIT, ButtonPermissionConstant.ROLE_DELETE,

                // 按钮权限 - 商品管理
                ButtonPermissionConstant.PRODUCT_ADD, ButtonPermissionConstant.PRODUCT_EDIT, ButtonPermissionConstant.PRODUCT_DELETE, ButtonPermissionConstant.PRODUCT_UPDATE,

                // 按钮权限 - 仓库管理
                ButtonPermissionConstant.WAREHOUSE_ADD, ButtonPermissionConstant.WAREHOUSE_EDIT, ButtonPermissionConstant.WAREHOUSE_DELETE,

                // 按钮权限 - 库存管理
                ButtonPermissionConstant.INVENTORY_UPDATE,

                // 按钮权限 - 采购管理
                ButtonPermissionConstant.PURCHASE_ORDER_ADD, ButtonPermissionConstant.PURCHASE_ORDER_SHIP, ButtonPermissionConstant.PURCHASE_ORDER_STOCK_IN, ButtonPermissionConstant.PURCHASE_RETURN_ADD, ButtonPermissionConstant.PURCHASE_RETURN_CONFIRM,

                // 按钮权限 - 销售管理
                ButtonPermissionConstant.SALE_ORDER_CREATE, ButtonPermissionConstant.SALE_ORDER_CONFIRM, ButtonPermissionConstant.INVENTORY_SALE_ORDER_SHIP, ButtonPermissionConstant.SALE_RETURN_ADD,

                // 用户接口权限
                UserInterfaceConstant.USER_LIST_GET,
                UserInterfaceConstant.USER_DETAIL_GET,
                UserInterfaceConstant.USER_CREATE_POST,
                UserInterfaceConstant.USER_UPDATE_PUT,
                UserInterfaceConstant.USER_RESET_PASSWORD_POST,
                UserInterfaceConstant.USER_BATCH_STATUS_POST,
                UserInterfaceConstant.USER_DELETE_DELETE,
                UserInterfaceConstant.USER_LOGIN_POST,
                UserInterfaceConstant.USER_REGISTER_POST,

                // 仓库接口权限
                WarehouseInterfaceConstant.WAREHOUSE_ADD_POST,
                WarehouseInterfaceConstant.WAREHOUSE_DETAIL_GET,
                WarehouseInterfaceConstant.WAREHOUSE_UPDATE_PUT,
                WarehouseInterfaceConstant.WAREHOUSE_DELETE_DELETE,
                WarehouseInterfaceConstant.WAREHOUSE_LIST_GET,
                WarehouseInterfaceConstant.WAREHOUSE_PAGE_GET,

                // 门店接口权限
                StoreInterfaceConstant.STORE_DETAIL_GET,
                StoreInterfaceConstant.STORE_UPDATE_PUT,

                // 销售退货接口权限
                SaleReturnInterfaceConstant.SALE_RETURN_CREATE_POST,
                SaleReturnInterfaceConstant.SALE_RETURN_MY_GET,
                SaleReturnInterfaceConstant.SALE_RETURN_STORE_GET,
                SaleReturnInterfaceConstant.SALE_RETURN_DETAIL_GET,

                // 销售订单接口权限
                SaleOrderInterfaceConstant.SALE_ORDER_CREATE_POST,
                SaleOrderInterfaceConstant.SALE_ORDER_MY_GET,
                SaleOrderInterfaceConstant.SALE_ORDER_DETAIL_GET,
                SaleOrderInterfaceConstant.SALE_ORDER_STORE_GET,
                SaleOrderInterfaceConstant.SALE_ORDER_CONFIRM_POST,

                // 角色接口权限
                RoleInterfaceConstant.ROLE_LIST_GET,
                RoleInterfaceConstant.ROLE_CREATE_POST,
                RoleInterfaceConstant.ROLE_DETAIL_GET,
                RoleInterfaceConstant.ROLE_UPDATE_PUT,
                RoleInterfaceConstant.ROLE_DELETE_DELETE,
                RoleInterfaceConstant.ROLE_ASSIGN_PERMISSION_PUT,
                RoleInterfaceConstant.ROLE_ASSIGN_PAGE_POST,

                // 采退订单接口权限
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_LIST_GET,
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_CREATE_POST,
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_CONFIRM_POST,

                // 采购订单接口权限
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_CREATE_POST,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_LIST_GET,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_DETAIL_GET,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_SHIP_POST,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_SUPPLIER_LIST_GET,

                // 商品接口权限
                ProductInterfaceConstant.PRODUCT_ADD_POST,
                ProductInterfaceConstant.PRODUCT_UPDATE_PUT,
                ProductInterfaceConstant.PRODUCT_DELETE_DELETE,
                ProductInterfaceConstant.PRODUCT_DETAIL_GET,
                ProductInterfaceConstant.PRODUCT_LIST_GET,
                ProductInterfaceConstant.PRODUCT_LIST_OWN_GET,

                // 权限接口权限
                PermissionInterfaceConstant.PERMISSION_LIST_GET,
                PermissionInterfaceConstant.PERMISSION_CREATE_POST,
                PermissionInterfaceConstant.PERMISSION_UPDATE_PUT,
                PermissionInterfaceConstant.PERMISSION_DELETE_DELETE,
                PermissionInterfaceConstant.PERMISSION_DETAIL_GET,
                PermissionInterfaceConstant.PERMISSION_USER_GET,

                // 页面接口权限
                PageInterfaceConstant.PAGE_LIST_GET,
                PageInterfaceConstant.PAGE_DETAIL_GET,
                PageInterfaceConstant.PAGE_CREATE_POST,
                PageInterfaceConstant.PAGE_UPDATE_PUT,
                PageInterfaceConstant.PAGE_DELETE_DELETE,
                PageInterfaceConstant.PAGE_ALL_GET,
                PageInterfaceConstant.PAGE_ASSIGN_SINGLE_PERMISSION_POST,
                PageInterfaceConstant.PAGE_ASSIGN_BATCH_PERMISSION_PUT,
                PageInterfaceConstant.PAGE_USER_GET,

                // 操作日志接口权限
                OperationLogInterfaceConstant.OPERATION_LOG_PAGE_POST,

                // 库存接口权限
                InventoryInterfaceConstant.INVENTORY_LIST_GET,
                InventoryInterfaceConstant.INVENTORY_DETAIL_GET,
                InventoryInterfaceConstant.INVENTORY_UPDATE_POST,
                InventoryInterfaceConstant.INVENTORY_STOCK_IN_POST,
                InventoryInterfaceConstant.INVENTORY_SALE_ORDER_POST,
                InventoryInterfaceConstant.INVENTORY_SALE_RETURN_CONFIRM_POST,

                // 库存明细接口权限
                InventoryDetailInterfaceConstant.INVENTORY_DETAIL_LIST_GET,
                InventoryDetailInterfaceConstant.INVENTORY_DETAIL_DETAIL_GET,

                // 金额单接口权限
                AmountOrderInterfaceConstant.AMOUNT_ORDER_LIST_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_LIST_PAYER_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_LIST_PAYEE_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_DETAIL_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_PAY_POST,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_CANCEL_POST,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_NOTIFY_POST
        );
        Map<String, Permission> existingPermsByName = permissionService.list(
                new LambdaQueryWrapper<Permission>().in(Permission::getName, basePermNames)
        ).stream().collect(Collectors.toMap(Permission::getName, p -> p, (a, b) -> a));

        for (String permName : basePermNames) {
            if (existingPermsByName.containsKey(permName)) {
                continue;
            }
            Permission permission = new Permission();
            permission.setName(permName);
            permission.setDescription(permName);
            permission.setCreateBy(createBy);
            permissionService.save(permission);
        }

        List<Permission> perms = permissionService.list(
                new LambdaQueryWrapper<Permission>().in(Permission::getName, basePermNames)
        );
        
        // 7. 分配权限给超级管理员
        assignPermissionsToRole(adminRole, perms, createBy);

        // 8. 分配权限给门店管理员
        if (storeAdminRole != null) {
            List<String> storeAdminPermNames = List.of(
                // 按钮权限 - 用户管理
                ButtonPermissionConstant.USER_ADD, ButtonPermissionConstant.USER_EDIT, ButtonPermissionConstant.USER_DELETE, ButtonPermissionConstant.USER_RESET_PASSWORD,

                // 按钮权限 - 商品管理
                ButtonPermissionConstant.PRODUCT_ADD, ButtonPermissionConstant.PRODUCT_EDIT, ButtonPermissionConstant.PRODUCT_DELETE, ButtonPermissionConstant.PRODUCT_UPDATE,

                // 按钮权限 - 仓库管理
                ButtonPermissionConstant.WAREHOUSE_ADD, ButtonPermissionConstant.WAREHOUSE_EDIT, ButtonPermissionConstant.WAREHOUSE_DELETE,

                // 按钮权限 - 库存管理
                ButtonPermissionConstant.INVENTORY_UPDATE,

                // 按钮权限 - 采购管理
                ButtonPermissionConstant.PURCHASE_ORDER_ADD, ButtonPermissionConstant.PURCHASE_ORDER_STOCK_IN, ButtonPermissionConstant.PURCHASE_RETURN_ADD, ButtonPermissionConstant.PURCHASE_RETURN_CONFIRM,

                // 按钮权限 - 销售管理
                ButtonPermissionConstant.SALE_ORDER_CREATE, ButtonPermissionConstant.SALE_ORDER_CONFIRM, ButtonPermissionConstant.INVENTORY_SALE_ORDER_SHIP, ButtonPermissionConstant.SALE_RETURN_ADD,

                // 用户接口权限
                UserInterfaceConstant.USER_LIST_GET,
                UserInterfaceConstant.USER_DETAIL_GET,
                UserInterfaceConstant.USER_CREATE_POST,
                UserInterfaceConstant.USER_UPDATE_PUT,
                UserInterfaceConstant.USER_RESET_PASSWORD_POST,
                UserInterfaceConstant.USER_BATCH_STATUS_POST,
                UserInterfaceConstant.USER_DELETE_DELETE,

                // 仓库接口权限
                WarehouseInterfaceConstant.WAREHOUSE_ADD_POST,
                WarehouseInterfaceConstant.WAREHOUSE_DETAIL_GET,
                WarehouseInterfaceConstant.WAREHOUSE_UPDATE_PUT,
                WarehouseInterfaceConstant.WAREHOUSE_DELETE_DELETE,
                WarehouseInterfaceConstant.WAREHOUSE_LIST_GET,
                WarehouseInterfaceConstant.WAREHOUSE_PAGE_GET,

                // 门店接口权限
                StoreInterfaceConstant.STORE_DETAIL_GET,
                // StoreInterfaceConstant.STORE_UPDATE_PUT, // 门店管理员暂时不修改门店信息，或者可以？先不给

                // 销售退货接口权限
                SaleReturnInterfaceConstant.SALE_RETURN_CREATE_POST,
                SaleReturnInterfaceConstant.SALE_RETURN_MY_GET,
                SaleReturnInterfaceConstant.SALE_RETURN_STORE_GET,
                SaleReturnInterfaceConstant.SALE_RETURN_DETAIL_GET,

                // 销售订单接口权限
                SaleOrderInterfaceConstant.SALE_ORDER_CREATE_POST,
                SaleOrderInterfaceConstant.SALE_ORDER_MY_GET,
                SaleOrderInterfaceConstant.SALE_ORDER_DETAIL_GET,
                SaleOrderInterfaceConstant.SALE_ORDER_STORE_GET,
                SaleOrderInterfaceConstant.SALE_ORDER_CONFIRM_POST,

                // 采退订单接口权限
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_LIST_GET,
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_CREATE_POST,
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_CONFIRM_POST,

                // 采购订单接口权限
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_CREATE_POST,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_LIST_GET,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_DETAIL_GET,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_SUPPLIER_LIST_GET,

                // 商品接口权限
                ProductInterfaceConstant.PRODUCT_ADD_POST,
                ProductInterfaceConstant.PRODUCT_UPDATE_PUT,
                ProductInterfaceConstant.PRODUCT_DELETE_DELETE,
                ProductInterfaceConstant.PRODUCT_DETAIL_GET,
                ProductInterfaceConstant.PRODUCT_LIST_GET,
                ProductInterfaceConstant.PRODUCT_LIST_OWN_GET,

                // 库存接口权限
                InventoryInterfaceConstant.INVENTORY_LIST_GET,
                InventoryInterfaceConstant.INVENTORY_DETAIL_GET,
                InventoryInterfaceConstant.INVENTORY_UPDATE_POST,
                InventoryInterfaceConstant.INVENTORY_STOCK_IN_POST,
                InventoryInterfaceConstant.INVENTORY_SALE_ORDER_POST,
                InventoryInterfaceConstant.INVENTORY_SALE_RETURN_CONFIRM_POST,

                // 库存明细接口权限
                InventoryDetailInterfaceConstant.INVENTORY_DETAIL_LIST_GET,
                InventoryDetailInterfaceConstant.INVENTORY_DETAIL_DETAIL_GET,

                // 金额单接口权限
                AmountOrderInterfaceConstant.AMOUNT_ORDER_LIST_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_LIST_PAYER_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_LIST_PAYEE_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_DETAIL_GET,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_PAY_POST,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_CANCEL_POST,
                AmountOrderInterfaceConstant.AMOUNT_ORDER_NOTIFY_POST
            );

            List<Permission> storeAdminPerms = perms.stream()
                .filter(p -> storeAdminPermNames.contains(p.getName()))
                .collect(Collectors.toList());
            assignPermissionsToRole(storeAdminRole, storeAdminPerms, createBy);
        }

        // 9. 分配权限给供应商
        if (supplierRole != null) {
            List<String> supplierPermNames = List.of(
                // 按钮权限
                ButtonPermissionConstant.PURCHASE_ORDER_SHIP, ButtonPermissionConstant.PURCHASE_RETURN_CONFIRM,

                // 接口权限
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_SHIP_POST,
                PurchaseOrderInterfaceConstant.PURCHASE_ORDER_SUPPLIER_LIST_GET,
                
                // 采退订单接口权限
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_LIST_GET,
                PurchaseReturnInterfaceConstant.PURCHASE_RETURN_CONFIRM_POST,

                // 支付相关接口
                AmountOrderInterfaceConstant.AMOUNT_ORDER_PAY_POST,

                // 商品管理相关
                ButtonPermissionConstant.PRODUCT_ADD, ButtonPermissionConstant.PRODUCT_EDIT, ButtonPermissionConstant.PRODUCT_DELETE, ButtonPermissionConstant.PRODUCT_UPDATE,
                ProductInterfaceConstant.PRODUCT_ADD_POST,
                ProductInterfaceConstant.PRODUCT_UPDATE_PUT,
                ProductInterfaceConstant.PRODUCT_DELETE_DELETE,
                ProductInterfaceConstant.PRODUCT_DETAIL_GET,
                ProductInterfaceConstant.PRODUCT_LIST_OWN_GET,
                
                // 销售退货相关 (供应商可能需要查看销售退货申请)
                SaleReturnInterfaceConstant.SALE_RETURN_MY_GET,
                SaleReturnInterfaceConstant.SALE_RETURN_DETAIL_GET
            );
            List<Permission> supplierPerms = perms.stream()
                .filter(p -> supplierPermNames.contains(p.getName()))
                .collect(Collectors.toList());
            assignPermissionsToRole(supplierRole, supplierPerms, createBy);
        }
    }

    private void assignPagesToRole(Role role, List<PageInfo> pages, Long createBy) {
        Set<Long> pageIds = pages.stream().map(PageInfo::getId).collect(Collectors.toSet());
        Set<Long> existingRolePageIds = roleRelatedPageService.list(
                new LambdaQueryWrapper<RoleRelatedPage>()
                        .eq(RoleRelatedPage::getRoleId, role.getId())
                        .in(RoleRelatedPage::getPageId, pageIds)
        ).stream().map(RoleRelatedPage::getPageId).collect(Collectors.toSet());

        List<RoleRelatedPage> toSave = pageIds.stream()
                .filter(pid -> !existingRolePageIds.contains(pid))
                .map(pid -> {
                    RoleRelatedPage rp = new RoleRelatedPage();
                    rp.setRoleId(role.getId());
                    rp.setPageId(pid);
                    rp.setCreateBy(createBy);
                    return rp;
                })
                .toList();
        if (!toSave.isEmpty()) {
            roleRelatedPageService.saveBatch(toSave);
        }
    }

    private void assignPermissionsToRole(Role role, List<Permission> perms, Long createBy) {
        Set<Long> permIds = perms.stream().map(Permission::getId).collect(Collectors.toSet());
        Set<Long> existingRolePermIds = roleRelatedPermissionService.list(
                new LambdaQueryWrapper<RoleRelatedPermission>()
                        .eq(RoleRelatedPermission::getRoleId, role.getId())
                        .in(RoleRelatedPermission::getPermissionId, permIds)
        ).stream().map(RoleRelatedPermission::getPermissionId).collect(Collectors.toSet());

        List<RoleRelatedPermission> toSave = permIds.stream()
                .filter(pid -> !existingRolePermIds.contains(pid))
                .map(pid -> {
                    RoleRelatedPermission rp = new RoleRelatedPermission();
                    rp.setRoleId(role.getId());
                    rp.setPermissionId(pid);
                    rp.setCreateBy(createBy);
                    return rp;
                })
                .toList();
        if (!toSave.isEmpty()) {
            roleRelatedPermissionService.saveBatch(toSave);
        }
    }
}
