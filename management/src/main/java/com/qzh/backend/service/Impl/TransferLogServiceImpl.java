package com.qzh.backend.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.config.AppGlobalConfig;
import com.qzh.backend.mapper.TransferLogMapper;
import com.qzh.backend.model.dto.transfer.TransferLogQueryDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.vo.TransferLogVO;
import com.qzh.backend.service.*;
import com.qzh.backend.utils.GetLoginUserUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferLogServiceImpl extends ServiceImpl<TransferLogMapper, TransferLog> implements TransferLogService {

    private final AppGlobalConfig appGlobalConfig;
    private final GetLoginUserUtil getLoginUserUtil;
    private final InventoryService inventoryService;
    private final WarehouseService warehouseService;
    private final ProductService productService;

    @Override
    public Page<TransferLogVO> listTransferLogs(TransferLogQueryDTO queryDTO, HttpServletRequest request) {
        long current = queryDTO.getCurrent();
        long size = queryDTO.getSize();

        User loginUser = getLoginUserUtil.getLoginUser(request);
        Long managerId = appGlobalConfig.getManagerId();
        
        QueryWrapper<TransferLog> queryWrapper = new QueryWrapper<>();
        
        // 如果是门店管理员，只能看本门店相关的调拨记录
        // 逻辑：本门店相关 = sourceWarehouseId 或 targetWarehouseId 属于本门店
        // 获取本门店所有仓库ID
        if (loginUser.getId().equals(managerId)) {
            Long storeId = appGlobalConfig.getCurrentStoreId();
            // 从库存表中查该门店的所有仓库ID
            List<Inventory> inventories = inventoryService.query().eq("storeId", storeId).select("warehouseId").list();
            Set<Long> warehouseIds = inventories.stream().map(Inventory::getWarehouseId).collect(Collectors.toSet());
            
            if (warehouseIds.isEmpty()) {
                return new Page<>(current, size);
            }
            
            queryWrapper.and(wrapper -> 
                wrapper.in("sourceWarehouseId", warehouseIds)
                       .or()
                       .in("targetWarehouseId", warehouseIds)
            );
        }
        
        queryWrapper.eq(ObjUtil.isNotEmpty(queryDTO.getSourceWarehouseId()), "sourceWarehouseId", queryDTO.getSourceWarehouseId());
        queryWrapper.eq(ObjUtil.isNotEmpty(queryDTO.getTargetWarehouseId()), "targetWarehouseId", queryDTO.getTargetWarehouseId());
        queryWrapper.eq(ObjUtil.isNotEmpty(queryDTO.getProductId()), "productId", queryDTO.getProductId());
        queryWrapper.ge(ObjUtil.isNotEmpty(queryDTO.getStartTime()), "createTime", queryDTO.getStartTime());
        queryWrapper.lt(ObjUtil.isNotEmpty(queryDTO.getEndTime()), "createTime", queryDTO.getEndTime());
        queryWrapper.orderByDesc("createTime");

        Page<TransferLog> page = this.page(new Page<>(current, size), queryWrapper);
        
        // Convert to VO
        List<TransferLogVO> voList = page.getRecords().stream().map(log -> {
            TransferLogVO vo = BeanUtil.copyProperties(log, TransferLogVO.class);
            // 填充名称
            Warehouse source = warehouseService.getById(log.getSourceWarehouseId());
            if (source != null) vo.setSourceWarehouseName(source.getName());
            
            Warehouse target = warehouseService.getById(log.getTargetWarehouseId());
            if (target != null) vo.setTargetWarehouseName(target.getName());
            
            Product product = productService.getById(log.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                vo.setProductUrl(product.getUrl());
            }
            
            return vo;
        }).collect(Collectors.toList());

        Page<TransferLogVO> resultPage = new Page<>(current, size, page.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
}