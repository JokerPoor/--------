package com.qzh.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.annotation.AuthCheck;
import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.common.BaseResponse;
import com.qzh.backend.common.ResultUtils;
import com.qzh.backend.model.dto.product.InventoryQueryDTO;
import com.qzh.backend.model.dto.product.InventoryUpdateDTO;
import com.qzh.backend.model.dto.product.MultiWarehouseStockInDTO;
import com.qzh.backend.model.vo.InventoryVO;
import com.qzh.backend.service.InventoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.qzh.backend.constants.Interface.InventoryInterfaceConstant.*;
import static com.qzh.backend.constants.ModuleConstant.INVENTORY_MODULE;

@RestController
@RequestMapping("inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 分页查询入库信息
     */
    @GetMapping("/list")
    @AuthCheck(interfaceName = INVENTORY_LIST_GET)
    public BaseResponse<Page<InventoryVO>> listInventories(InventoryQueryDTO queryDTO) {
        Page<InventoryVO> inventoryVOPage = inventoryService.listInventoriesWithQuantity(queryDTO);
        return ResultUtils.success(inventoryVOPage);
    }

    /**
     * 根据库存ID获取库存信息
     */
    @AuthCheck(interfaceName = INVENTORY_DETAIL_GET)
    @GetMapping("/{id}")
    public BaseResponse<InventoryVO> getInventoryById(@PathVariable Long id) {
        InventoryVO inventoryVO = inventoryService.getInventoryVOById(id);
        return ResultUtils.success(inventoryVO);
    }

    /**
     * 根据库存ID编辑库存中商品信息
     */
    @AuthCheck(interfaceName = INVENTORY_UPDATE_POST)
    @LogInfoRecord(SystemModule = INVENTORY_MODULE + ":" + INVENTORY_UPDATE_POST)
    @PostMapping("/update")
    public BaseResponse<Void> updateInventory(@Valid @RequestBody InventoryUpdateDTO updateDTO) {
        inventoryService.updateInventory(updateDTO);
        return ResultUtils.success(null);
    }


    @PostMapping("/stock-in")
    @AuthCheck(interfaceName = INVENTORY_STOCK_IN_POST)
    @LogInfoRecord(SystemModule = INVENTORY_MODULE + ":" + INVENTORY_STOCK_IN_POST)
    public BaseResponse<Void> stockIn(@Valid @RequestBody MultiWarehouseStockInDTO stockInDTO, HttpServletRequest request) {
        inventoryService.stockInNew(stockInDTO, request);
        return ResultUtils.success(null);
    }

    @PostMapping("/sale-order")
    @AuthCheck(interfaceName = INVENTORY_SALE_ORDER_POST)
    @LogInfoRecord(SystemModule = INVENTORY_MODULE + ":" + INVENTORY_SALE_ORDER_POST)
    public BaseResponse<Void>  saleOrder(@RequestBody Long saleOrderId,HttpServletRequest request) {
        inventoryService.saleOrder(saleOrderId, request);
        return ResultUtils.success(null);
    }

    @PostMapping("/sale-return/confirm")
    @AuthCheck(interfaceName = INVENTORY_SALE_RETURN_CONFIRM_POST)
    @LogInfoRecord(SystemModule = INVENTORY_MODULE + ":" + INVENTORY_SALE_RETURN_CONFIRM_POST)
    public BaseResponse<Void> confirmSaleReturn(@RequestBody Long saleReturnId, HttpServletRequest request) {
        inventoryService.confirmSaleReturn(saleReturnId, request);
        return ResultUtils.success(null);
    }

}
