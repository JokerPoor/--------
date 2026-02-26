package com.qzh.backend.controller;

import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Dashboard统计控制器
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取管理员统计数据
     */
    @GetMapping("/admin/stats")
    @AuthCheck(interfaceName = "dashboard:admin:stats")
    public BaseResponse<Map<String, Object>> getAdminStats() {
        Map<String, Object> stats = dashboardService.getAdminStats();
        return ResultUtils.success(stats);
    }

    /**
     * 获取门店管理员统计数据
     */
    @GetMapping("/store/stats")
    @AuthCheck(interfaceName = "dashboard:store:stats")
    public BaseResponse<Map<String, Object>> getStoreStats() {
        Map<String, Object> stats = dashboardService.getStoreStats();
        return ResultUtils.success(stats);
    }

    /**
     * 获取供应商统计数据
     */
    @GetMapping("/supplier/stats")
    @AuthCheck(interfaceName = "dashboard:supplier:stats")
    public BaseResponse<Map<String, Object>> getSupplierStats() {
        Map<String, Object> stats = dashboardService.getSupplierStats();
        return ResultUtils.success(stats);
    }

    /**
     * 获取客户统计数据
     */
    @GetMapping("/customer/stats")
    @AuthCheck(interfaceName = "dashboard:customer:stats")
    public BaseResponse<Map<String, Object>> getCustomerStats() {
        Map<String, Object> stats = dashboardService.getCustomerStats();
        return ResultUtils.success(stats);
    }

    /**
     * 获取快捷统计数据（根据角色自动返回）
     */
    @GetMapping("/quick-stats")
    public BaseResponse<Map<String, Object>> getQuickStats() {
        Map<String, Object> stats = dashboardService.getQuickStats();
        return ResultUtils.success(stats);
    }
}
