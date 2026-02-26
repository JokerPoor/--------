package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.mapper.*;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.service.DashboardService;
import com.qzh.backend.utils.GetLoginUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Dashboard统计服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final OperationLogMapper operationLogMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryDetailMapper inventoryDetailMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseReturnMapper purchaseReturnMapper;
    private final SaleReturnMapper saleReturnMapper;
    private final ProductMapper productMapper;
    private final AmountOrderMapper amountOrderMapper;
    private final GetLoginUserUtil getLoginUserUtil;

    @Override
    public Map<String, Object> getAdminStats() {
        Map<String, Object> result = new HashMap<>();

        // Quick stats
        result.put("totalUsers", userMapper.selectCount(null));
        result.put("todayOrders", getTodayOrderCount());
        result.put("totalRevenue", getTotalRevenueNumber());
        result.put("activeUsers", userMapper.selectCount(null)); // 简化实现
        
        // 用户统计趋势
        result.put("userTrend", getUserTrend());
        
        // 角色分布
        result.put("roleDistribution", getRoleDistribution());
        
        // 操作日志趋势
        result.put("operationTrend", getOperationTrend());
        
        // 模块访问统计
        result.put("moduleAccess", getModuleAccess());

        return result;
    }

    @Override
    public Map<String, Object> getStoreStats() {
        Map<String, Object> result = new HashMap<>();

        // Quick stats
        result.put("totalProducts", inventoryMapper.selectCount(null));
        result.put("pendingOrders", getPendingOrderCount());
        result.put("warningProducts", getWarningProductCount());
        
        // 库存预警
        result.put("inventoryWarning", getInventoryWarning());
        
        // 销售趋势
        result.put("salesTrend", getSalesTrend());
        
        // 采购订单状态
        result.put("purchaseStatus", getPurchaseOrderStatus());
        
        // 热销商品TOP10
        result.put("topProducts", getTopProducts());

        return result;
    }

    @Override
    public Map<String, Object> getSupplierStats() {
        Map<String, Object> result = new HashMap<>();
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        User loginUser = getLoginUserUtil.getLoginUser(request);

        // Quick stats
        result.put("pendingOrders", getSupplierPendingShipment(loginUser.getId()));
        result.put("monthlyIncome", getSupplierCurrentMonthIncomeNumber(loginUser.getId()));
        result.put("totalProducts", getSupplierProductCount(loginUser.getId()));
        
        // 供应商订单统计
        result.put("orderStatus", getSupplierOrderStats(loginUser.getId()));
        
        // 订单金额趋势
        result.put("amountTrend", getSupplierAmountTrend(loginUser.getId()));
        
        // 商品销售排行
        result.put("productRanking", getSupplierProductRanking(loginUser.getId()));
        
        // 月度收入趋势图表
        result.put("monthlyIncomeTrend", getSupplierMonthlyIncome(loginUser.getId()));

        return result;
    }

    @Override
    public Map<String, Object> getCustomerStats() {
        Map<String, Object> result = new HashMap<>();
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        User loginUser = getLoginUserUtil.getLoginUser(request);

        // Quick stats
        result.put("totalOrders", getCustomerOrderCount(loginUser.getId()));
        result.put("monthlySpend", getCustomerCurrentMonthSpendNumber(loginUser.getId()));
        
        // 客户订单统计
        result.put("orderStatus", getCustomerOrderStats(loginUser.getId()));
        
        // 消费趋势
        result.put("spendTrend", getCustomerSpendTrend(loginUser.getId()));
        
        // 购买商品分类
        result.put("categoryStats", getCustomerCategoryStats(loginUser.getId()));
        
        // 月度消费趋势图表
        result.put("monthlySpendTrend", getCustomerMonthlySpend(loginUser.getId()));

        return result;
    }

    @Override
    public Map<String, Object> getQuickStats() {
        Map<String, Object> result = new HashMap<>();
        
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        User loginUser = getLoginUserUtil.getLoginUser(request);
        List<Role> roles = getLoginUserUtil.getRoleList(loginUser.getId());
        
        boolean isAdmin = roles.stream().anyMatch(r -> "超级管理员".equals(r.getRoleName()));
        boolean isStoreAdmin = roles.stream().anyMatch(r -> "门店管理员".equals(r.getRoleName()));
        boolean isSupplier = roles.stream().anyMatch(r -> "供应商".equals(r.getRoleName()));
        boolean isCustomer = roles.stream().anyMatch(r -> "客户".equals(r.getRoleName()));

        if (isAdmin) {
            result.put("totalUsers", userMapper.selectCount(null));
            result.put("todayOrders", getTodayOrderCount());
            result.put("totalRevenue", getTotalRevenue());
        } else if (isStoreAdmin) {
            result.put("totalInventory", inventoryMapper.selectCount(null));
            result.put("pendingOrders", getPendingOrderCount());
            result.put("warningProducts", getWarningProductCount());
        } else if (isSupplier) {
            result.put("pendingShipment", getSupplierPendingShipment(loginUser.getId()));
            result.put("monthlyIncome", getSupplierCurrentMonthIncome(loginUser.getId()));
            result.put("productCount", getSupplierProductCount(loginUser.getId()));
        } else if (isCustomer) {
            result.put("myOrders", getCustomerOrderCount(loginUser.getId()));
            result.put("monthlySpend", getCustomerCurrentMonthSpend(loginUser.getId()));
        }

        return result;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 用户统计趋势（最近7天）
     */
    private List<Map<String, Object>> getUserTrend() {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> item = new HashMap<>();
            item.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            // 统计当天新增用户
            Long newUsers = userMapper.selectCount(
                new QueryWrapper<User>()
                    .ge("createTime", startOfDay)
                    .lt("createTime", endOfDay)
            );
            
            // 统计当天活跃用户（有操作日志的不同用户数）
            List<OperationLog> logs = operationLogMapper.selectList(
                new QueryWrapper<OperationLog>()
                    .select("DISTINCT operatorId")
                    .ge("operationTime", startOfDay)
                    .lt("operationTime", endOfDay)
            );
            Long activeUsers = logs.stream()
                .map(OperationLog::getOperatorId)
                .filter(id -> id != null)
                .distinct()
                .count();
            
            item.put("newUsers", newUsers.intValue());
            item.put("activeUsers", activeUsers.intValue());
            
            result.add(item);
        }

        return result;
    }

    /**
     * 角色分布
     */
    private List<Map<String, Object>> getRoleDistribution() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Role> roles = roleMapper.selectList(null);
        
        for (Role role : roles) {
            Map<String, Object> item = new HashMap<>();
            item.put("roleName", role.getRoleName());
            // 统计该角色的用户数
            Long count = userMapper.selectCount(
                new QueryWrapper<User>()
                    .inSql("id", "SELECT userId FROM sys_user_role WHERE roleId = " + role.getId())
            );
            item.put("count", count);
            result.add(item);
        }
        
        return result;
    }

    /**
     * 操作日志趋势（最近6个月）
     */
    private List<Map<String, Object>> getOperationTrend() {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            Map<String, Object> item = new HashMap<>();
            item.put("month", month.format(DateTimeFormatter.ofPattern("MM月")));
            
            // 统计该月操作日志数量
            Long count = operationLogMapper.selectCount(
                new QueryWrapper<OperationLog>()
                    .ge("operationTime", month.withDayOfMonth(1).atStartOfDay())
                    .lt("operationTime", month.plusMonths(1).withDayOfMonth(1).atStartOfDay())
            );
            item.put("count", count.intValue());
            
            result.add(item);
        }

        return result;
    }

    /**
     * 模块访问统计
     */
    private List<Map<String, Object>> getModuleAccess() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        String[] modules = {"用户管理", "库存管理", "订单管理", "财务管理", "系统设置"};
        for (String module : modules) {
            Map<String, Object> item = new HashMap<>();
            item.put("moduleName", module);
            // 统计该模块的访问次数
            Long count = operationLogMapper.selectCount(
                new QueryWrapper<OperationLog>().like("systemModule", module)
            );
            item.put("count", count);
            result.add(item);
        }
        
        return result;
    }


    /**
     * 库存预警统计
     */
    private List<Map<String, Object>> getInventoryWarning() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 获取所有库存记录
        List<Inventory> inventoryList = inventoryMapper.selectList(null);
        if (inventoryList.isEmpty()) {
            result.add(createStatusItem("正常库存", 0L));
            result.add(createStatusItem("低库存预警", 0L));
            result.add(createStatusItem("缺货", 0L));
            return result;
        }
        
        // 获取所有库存明细并计算实时库存
        List<String> productWarehouseKeys = inventoryList.stream()
                .map(inv -> String.format("%d_%d", inv.getProductId(), inv.getWarehouseId()))
                .distinct()
                .toList();
        
        List<InventoryDetail> detailList = inventoryDetailMapper.selectList(
                new QueryWrapper<InventoryDetail>()
                        .in("CONCAT(productId, '_', warehouseId)", productWarehouseKeys)
        );
        
        Map<String, Integer> quantityMap = calculateQuantityByProductWarehouse(detailList);
        
        // 统计各状态库存数量
        long normalCount = 0;
        long lowCount = 0;
        long outOfStockCount = 0;
        
        for (Inventory inventory : inventoryList) {
            String key = String.format("%d_%d", inventory.getProductId(), inventory.getWarehouseId());
            Integer quantity = quantityMap.getOrDefault(key, 0);
            Integer threshold = inventory.getWarningThreshold();
            
            if (threshold == null) {
                threshold = 0;
            }
            
            if (quantity == 0) {
                outOfStockCount++;
            } else if (quantity < threshold) {
                lowCount++;
            } else {
                normalCount++;
            }
        }
        
        result.add(createStatusItem("正常库存", normalCount));
        result.add(createStatusItem("低库存预警", lowCount));
        result.add(createStatusItem("缺货", outOfStockCount));
        
        return result;
    }

    /**
     * 销售趋势（最近6个月）
     */
    private List<Map<String, Object>> getSalesTrend() {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            Map<String, Object> item = new HashMap<>();
            item.put("month", month.format(DateTimeFormatter.ofPattern("MM月")));
            
            // 统计该月销售额和销售量
            List<SaleOrder> orders = saleOrderMapper.selectList(
                new QueryWrapper<SaleOrder>()
                    .ge("createTime", month.withDayOfMonth(1).atStartOfDay())
                    .lt("createTime", month.plusMonths(1).withDayOfMonth(1).atStartOfDay())
            );
            
            BigDecimal monthAmount = orders.stream()
                .map(SaleOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Integer monthQuantity = orders.stream()
                .mapToInt(SaleOrder::getProductQuantity)
                .sum();
            
            item.put("amount", monthAmount);
            item.put("quantity", monthQuantity);
            
            result.add(item);
        }

        return result;
    }

    /**
     * 采购订单状态统计
     */
    private List<Map<String, Object>> getPurchaseOrderStatus() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        Long pending = purchaseOrderMapper.selectCount(
            new QueryWrapper<PurchaseOrder>().eq("status", 0)
        );
        Long shipped = purchaseOrderMapper.selectCount(
            new QueryWrapper<PurchaseOrder>().eq("status", 1)
        );
        Long received = purchaseOrderMapper.selectCount(
            new QueryWrapper<PurchaseOrder>().eq("status", 2)
        );
        
        result.add(createStatusItem("待发货", pending));
        result.add(createStatusItem("已发货", shipped));
        result.add(createStatusItem("已入库", received));
        
        return result;
    }

    /**
     * 热销商品TOP10
     */
    private List<Map<String, Object>> getTopProducts() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询所有销售订单，按商品统计销量
        List<SaleOrder> orders = saleOrderMapper.selectList(null);
        Map<Long, Integer> productSales = new HashMap<>();
        Map<Long, String> productNames = new HashMap<>();
        
        for (SaleOrder order : orders) {
            Long productId = order.getProductId();
            productSales.put(productId, productSales.getOrDefault(productId, 0) + order.getProductQuantity());
            if (!productNames.containsKey(productId)) {
                Product product = productMapper.selectById(productId);
                if (product != null) {
                    productNames.put(productId, product.getName());
                }
            }
        }
        
        // 排序并取TOP10
        productSales.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("productName", productNames.getOrDefault(entry.getKey(), "商品" + entry.getKey()));
                item.put("sales", entry.getValue());
                result.add(item);
            });
        
        return result;
    }

    /**
     * 供应商订单统计
     */
    private List<Map<String, Object>> getSupplierOrderStats(Long supplierId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 注意：这里假设PurchaseOrder有supplier_id字段，如果没有需要调整逻辑
        Long pending = purchaseOrderMapper.selectCount(
            new QueryWrapper<PurchaseOrder>()
                .eq("status", 0)
        );
        Long shipped = purchaseOrderMapper.selectCount(
            new QueryWrapper<PurchaseOrder>()
                .eq("status", 1)
        );
        Long completed = purchaseOrderMapper.selectCount(
            new QueryWrapper<PurchaseOrder>()
                .eq("status", 2)
        );
        
        result.add(createStatusItem("待发货", pending));
        result.add(createStatusItem("已发货", shipped));
        result.add(createStatusItem("已完成", completed));
        
        return result;
    }

    /**
     * 供应商订单金额趋势
     */
    private List<Map<String, Object>> getSupplierAmountTrend(Long supplierId) {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            Map<String, Object> item = new HashMap<>();
            item.put("month", month.format(DateTimeFormatter.ofPattern("MM月")));
            
            List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new QueryWrapper<PurchaseOrder>()
                    .ge("createTime", month.withDayOfMonth(1).atStartOfDay())
                    .lt("createTime", month.plusMonths(1).withDayOfMonth(1).atStartOfDay())
            );
            
            BigDecimal monthAmount = orders.stream()
                .map(PurchaseOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            item.put("amount", monthAmount);
            result.add(item);
        }

        return result;
    }

    /**
     * 供应商商品销售排行
     */
    private List<Map<String, Object>> getSupplierProductRanking(Long supplierId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 查询供应商的商品销售情况
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(null);
        Map<Long, Integer> productSales = new HashMap<>();
        Map<Long, String> productNames = new HashMap<>();
        
        for (PurchaseOrder order : orders) {
            Long productId = order.getProductId();
            productSales.put(productId, productSales.getOrDefault(productId, 0) + order.getProductQuantity());
            if (!productNames.containsKey(productId)) {
                Product product = productMapper.selectById(productId);
                if (product != null) {
                    productNames.put(productId, product.getName());
                }
            }
        }
        
        productSales.entrySet().stream()
            .sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                Map<String, Object> item = new HashMap<>();
                item.put("productName", productNames.getOrDefault(entry.getKey(), "商品" + entry.getKey()));
                item.put("sales", entry.getValue());
                result.add(item);
            });
        
        return result;
    }

    /**
     * 供应商月度收入统计
     */
    private List<Map<String, Object>> getSupplierMonthlyIncome(Long supplierId) {
        return getSupplierAmountTrend(supplierId); // 复用金额趋势方法，返回格式相同
    }


    /**
     * 客户订单统计
     */
    private List<Map<String, Object>> getCustomerOrderStats(Long customerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        Long pending = saleOrderMapper.selectCount(
            new QueryWrapper<SaleOrder>()
                .eq("userId", customerId)
                .eq("status", 0)
        );
        Long shipped = saleOrderMapper.selectCount(
            new QueryWrapper<SaleOrder>()
                .eq("userId", customerId)
                .eq("status", 1)
        );
        Long completed = saleOrderMapper.selectCount(
            new QueryWrapper<SaleOrder>()
                .eq("userId", customerId)
                .eq("status", 2)
        );
        
        result.add(createStatusItem("待发货", pending));
        result.add(createStatusItem("已发货", shipped));
        result.add(createStatusItem("已完成", completed));
        
        return result;
    }

    /**
     * 客户消费趋势
     */
    private List<Map<String, Object>> getCustomerSpendTrend(Long customerId) {
        List<Map<String, Object>> result = new ArrayList<>();

        LocalDate today = LocalDate.now();
        for (int i = 5; i >= 0; i--) {
            LocalDate month = today.minusMonths(i);
            Map<String, Object> item = new HashMap<>();
            item.put("month", month.format(DateTimeFormatter.ofPattern("MM月")));
            
            List<SaleOrder> orders = saleOrderMapper.selectList(
                new QueryWrapper<SaleOrder>()
                    .eq("userId", customerId)
                    .ge("createTime", month.withDayOfMonth(1).atStartOfDay())
                    .lt("createTime", month.plusMonths(1).withDayOfMonth(1).atStartOfDay())
            );
            
            BigDecimal monthAmount = orders.stream()
                .map(SaleOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            item.put("amount", monthAmount);
            result.add(item);
        }

        return result;
    }

    /**
     * 客户购买商品分类统计
     */
    private List<Map<String, Object>> getCustomerCategoryStats(Long customerId) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // 简化实现:按商品名称前缀分类
        List<SaleOrder> orders = saleOrderMapper.selectList(
            new QueryWrapper<SaleOrder>().eq("userId", customerId)
        );
        
        Map<String, Integer> categoryCount = new HashMap<>();
        categoryCount.put("食品", 0);
        categoryCount.put("日用品", 0);
        categoryCount.put("饮料", 0);
        categoryCount.put("其他", 0);
        
        for (SaleOrder order : orders) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                String name = product.getName();
                if (name.contains("食品") || name.contains("零食")) {
                    categoryCount.put("食品", categoryCount.get("食品") + order.getProductQuantity());
                } else if (name.contains("日用") || name.contains("用品")) {
                    categoryCount.put("日用品", categoryCount.get("日用品") + order.getProductQuantity());
                } else if (name.contains("饮料") || name.contains("水")) {
                    categoryCount.put("饮料", categoryCount.get("饮料") + order.getProductQuantity());
                } else {
                    categoryCount.put("其他", categoryCount.get("其他") + order.getProductQuantity());
                }
            }
        }
        
        categoryCount.forEach((key, value) -> {
            if (value > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("category", key);
                item.put("count", value);
                result.add(item);
            }
        });
        
        return result;
    }

    /**
     * 客户月度消费统计
     */
    private List<Map<String, Object>> getCustomerMonthlySpend(Long customerId) {
        return getCustomerSpendTrend(customerId); // 复用消费趋势方法，返回格式相同
    }

    // ==================== 快捷统计辅助方法 ====================

    /**
     * 今日订单数
     */
    private Long getTodayOrderCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        return saleOrderMapper.selectCount(
            new QueryWrapper<SaleOrder>()
                .ge("createTime", startOfDay)
                .lt("createTime", endOfDay)
        );
    }

    /**
     * 总收入（数字）
     */
    private BigDecimal getTotalRevenueNumber() {
        List<AmountOrder> orders = amountOrderMapper.selectList(
            new QueryWrapper<AmountOrder>()
                .eq("type", 2) // 销售收款
                .eq("status", 1) // 已支付
        );
        
        return orders.stream()
            .map(AmountOrder::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 总收入
     */
    private String getTotalRevenue() {
        BigDecimal total = getTotalRevenueNumber();
        return "¥" + total.divide(new BigDecimal("1000"), 1, BigDecimal.ROUND_HALF_UP) + "K";
    }

    /**
     * 待处理订单数
     */
    private Long getPendingOrderCount() {
        return saleOrderMapper.selectCount(
            new QueryWrapper<SaleOrder>().eq("status", 0)
        );
    }

    /**
     * 预警商品数
     */
    private Long getWarningProductCount() {
        // 获取所有库存记录
        List<Inventory> inventoryList = inventoryMapper.selectList(null);
        if (inventoryList.isEmpty()) {
            return 0L;
        }
        
        // 获取所有库存明细并计算实时库存
        List<String> productWarehouseKeys = inventoryList.stream()
                .map(inv -> String.format("%d_%d", inv.getProductId(), inv.getWarehouseId()))
                .distinct()
                .toList();
        
        List<InventoryDetail> detailList = inventoryDetailMapper.selectList(
                new QueryWrapper<InventoryDetail>()
                        .in("CONCAT(productId, '_', warehouseId)", productWarehouseKeys)
        );
        
        Map<String, Integer> quantityMap = calculateQuantityByProductWarehouse(detailList);
        
        // 统计预警商品数量
        long warningCount = 0;
        for (Inventory inventory : inventoryList) {
            String key = String.format("%d_%d", inventory.getProductId(), inventory.getWarehouseId());
            Integer quantity = quantityMap.getOrDefault(key, 0);
            Integer threshold = inventory.getWarningThreshold();
            
            if (threshold != null && quantity < threshold) {
                warningCount++;
            }
        }
        
        return warningCount;
    }

    /**
     * 供应商待发货订单数
     */
    private Long getSupplierPendingShipment(Long supplierId) {
        return purchaseOrderMapper.selectCount(
            new QueryWrapper<PurchaseOrder>().eq("status", 0)
        );
    }

    /**
     * 供应商当月收入（数字）
     */
    private BigDecimal getSupplierCurrentMonthIncomeNumber(Long supplierId) {
        LocalDate today = LocalDate.now();
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
            new QueryWrapper<PurchaseOrder>()
                .ge("createTime", today.withDayOfMonth(1).atStartOfDay())
                .lt("createTime", today.plusMonths(1).withDayOfMonth(1).atStartOfDay())
        );
        
        return orders.stream()
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 供应商当月收入
     */
    private String getSupplierCurrentMonthIncome(Long supplierId) {
        BigDecimal total = getSupplierCurrentMonthIncomeNumber(supplierId);
        return "¥" + total.divide(new BigDecimal("1000"), 1, BigDecimal.ROUND_HALF_UP) + "K";
    }

    /**
     * 供应商商品数量
     */
    private Long getSupplierProductCount(Long supplierId) {
        // 简化：返回所有商品数
        return productMapper.selectCount(null);
    }

    /**
     * 客户订单数
     */
    private Long getCustomerOrderCount(Long customerId) {
        return saleOrderMapper.selectCount(
            new QueryWrapper<SaleOrder>().eq("userId", customerId)
        );
    }

    /**
     * 客户当月消费（数字）
     */
    private BigDecimal getCustomerCurrentMonthSpendNumber(Long customerId) {
        LocalDate today = LocalDate.now();
        List<SaleOrder> orders = saleOrderMapper.selectList(
            new QueryWrapper<SaleOrder>()
                .eq("userId", customerId)
                .ge("createTime", today.withDayOfMonth(1).atStartOfDay())
                .lt("createTime", today.plusMonths(1).withDayOfMonth(1).atStartOfDay())
        );
        
        return orders.stream()
            .map(SaleOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 客户当月消费
     */
    private String getCustomerCurrentMonthSpend(Long customerId) {
        BigDecimal total = getCustomerCurrentMonthSpendNumber(customerId);
        return "¥" + total.divide(new BigDecimal("1000"), 1, BigDecimal.ROUND_HALF_UP) + "K";
    }

    /**
     * 创建Map项的辅助方法（通用）
     */
    private Map<String, Object> createMapItem(String name, Object value) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", name);
        item.put("value", value);
        return item;
    }

    /**
     * 创建状态项的辅助方法
     */
    private Map<String, Object> createStatusItem(String status, Object count) {
        Map<String, Object> item = new HashMap<>();
        item.put("status", status);
        item.put("count", count);
        return item;
    }

    /**
     * 创建分类项的辅助方法
     */
    private Map<String, Object> createCategoryItem(String category, Object count) {
        Map<String, Object> item = new HashMap<>();
        item.put("category", category);
        item.put("count", count);
        return item;
    }

    /**
     * 计算商品在各仓库的实时库存数量
     * 根据库存明细表计算：入库 - 出库
     */
    private Map<String, Integer> calculateQuantityByProductWarehouse(List<InventoryDetail> detailList) {
        if (detailList == null || detailList.isEmpty()) {
            return Collections.emptyMap();
        }
        
        return detailList.stream()
                .collect(Collectors.groupingBy(
                        detail -> String.format("%d_%d", detail.getProductId(), detail.getWarehouseId()),
                        Collectors.summingInt(detail -> {
                            // type: 0-入库, 1-出库
                            Integer type = detail.getType();
                            Integer quantity = detail.getProductQuantity();
                            if (type == null || quantity == null) {
                                return 0;
                            }
                            return type == 0 ? quantity : -quantity;
                        })
                ));
    }
}
