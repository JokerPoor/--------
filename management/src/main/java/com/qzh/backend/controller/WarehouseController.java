package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.warehouse.WarehouseAddDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseQueryDTO;
import com.qzh.backend.model.dto.warehouse.WarehouseUpdateDTO;
import com.qzh.backend.model.entity.Warehouse;
import com.qzh.backend.service.WarehouseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.qzh.backend.constants.Interface.WarehouseInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.WAREHOUSE_MODULE;

/**
 * 仓库管理 Controller
 */
@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * 新增仓库
     */
    @PostMapping("/add")
    @AuthCheck(interfaceName = WAREHOUSE_ADD_POST)
    @LogInfoRecord(SystemModule = WAREHOUSE_MODULE + ":" + WAREHOUSE_ADD_POST)
    public BaseResponse<Long> addWarehouse(@Valid @RequestBody WarehouseAddDTO addDTO, HttpServletRequest request) {
        Long id = warehouseService.addWarehouse(addDTO, request);
        return ResultUtils.success(id);
    }

    /**
     * 根据 ID 查询仓库
     */
    @GetMapping("/get/{id}")
    @AuthCheck(interfaceName = WAREHOUSE_DETAIL_GET)
    public BaseResponse<Warehouse> getWarehouseById(@PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        return ResultUtils.success(warehouse);
    }

    /**
     * 更新仓库信息
     */
    @PutMapping("/update")
    @AuthCheck(interfaceName = WAREHOUSE_UPDATE_PUT)
    @LogInfoRecord(SystemModule = WAREHOUSE_MODULE + ":" + WAREHOUSE_UPDATE_PUT)
    public BaseResponse<Boolean> updateWarehouse(@Valid @RequestBody WarehouseUpdateDTO updateDTO) {
        boolean success = warehouseService.updateWarehouse(updateDTO);
        return ResultUtils.success(success);
    }

    /**
     * 删除仓库
     */
    @DeleteMapping("/delete/{id}")
    @AuthCheck(interfaceName = WAREHOUSE_DELETE_DELETE)
    @LogInfoRecord(SystemModule = WAREHOUSE_MODULE + ":" + WAREHOUSE_DELETE_DELETE)
    public BaseResponse<Boolean> deleteWarehouse(@PathVariable Long id) {
        boolean success = warehouseService.removeById(id);
        return ResultUtils.success(success);
    }

    /**
     * 分页查询仓库列表
     */
    @GetMapping("/list")
    @AuthCheck(interfaceName = WAREHOUSE_LIST_GET)
    public BaseResponse<Page<Warehouse>> getWarehouseList(@Valid WarehouseQueryDTO queryDTO) {
        Page<Warehouse> resultPage = warehouseService.getWarehousePage(queryDTO);
        return ResultUtils.success(resultPage);
    }

    /**
     * 分页查询仓库列表（兼容旧接口）
     */
    @GetMapping("/page")
    @AuthCheck(interfaceName = WAREHOUSE_PAGE_GET)
    public BaseResponse<Page<Warehouse>> getWarehousePage(@Valid WarehouseQueryDTO queryDTO) {
        Page<Warehouse> resultPage = warehouseService.getWarehousePage(queryDTO);
        return ResultUtils.success(resultPage);
    }
}