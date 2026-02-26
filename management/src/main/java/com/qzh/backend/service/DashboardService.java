package com.qzh.backend.service;

import java.util.Map;

/**
 * Dashboard统计服务接口
 */
public interface DashboardService {

    /**
     * 获取管理员统计数据
     */
    Map<String, Object> getAdminStats();

    /**
     * 获取门店管理员统计数据
     */
    Map<String, Object> getStoreStats();

    /**
     * 获取供应商统计数据
     */
    Map<String, Object> getSupplierStats();

    /**
     * 获取客户统计数据
     */
    Map<String, Object> getCustomerStats();

    /**
     * 获取快捷统计数据
     */
    Map<String, Object> getQuickStats();
}
