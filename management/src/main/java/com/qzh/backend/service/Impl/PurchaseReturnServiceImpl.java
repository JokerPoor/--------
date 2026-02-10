package com.qzh.backend.service.Impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzh.backend.exception.BusinessException;
import com.qzh.backend.exception.ErrorCode;
import com.qzh.backend.mapper.*;
import com.qzh.backend.model.dto.product.PurchaseReturnCreateDTO;
import com.qzh.backend.model.entity.*;
import com.qzh.backend.model.enums.*;
import com.qzh.backend.service.PurchaseReturnService;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.ThrowUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qzh.backend.model.dto.product.PurchaseReturnQueryDTO;

import com.qzh.backend.model.vo.PurchaseReturnVO;
import org.springframework.beans.BeanUtils;

@Service
@RequiredArgsConstructor
public class PurchaseReturnServiceImpl extends ServiceImpl<PurchaseReturnMapper, PurchaseReturn> implements PurchaseReturnService {

    private final InventoryDetailMapper inventoryDetailMapper;

    private final GetLoginUserUtil getLoginUserUtil;

    private final AmountOrderMapper amountOrderMapper;

    private final PurchaseOrderMapper purchaseOrderMapper;

    @Override
    public Page<PurchaseReturnVO> listPurchaseReturns(PurchaseReturnQueryDTO queryDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(queryDTO == null, ErrorCode.PARAMS_ERROR);
        int current = queryDTO.getCurrent();
        int size = queryDTO.getSize();
        ThrowUtils.throwIf(size <= 0 || size > 1000, ErrorCode.PARAMS_ERROR);
        
        Page<PurchaseReturn> page = new Page<>(current, size);
        Page<PurchaseReturn> purchaseReturnPage = this.page(page, PurchaseReturnQueryDTO.getQueryWrapper(queryDTO));
        
        Page<PurchaseReturnVO> voPage = new Page<>(current, size, purchaseReturnPage.getTotal());
        if (CollectionUtils.isEmpty(purchaseReturnPage.getRecords())) {
            return voPage;
        }

        // 收集ID并批量查询金额单
        List<PurchaseReturn> records = purchaseReturnPage.getRecords();
        List<String> orderIdStrs = records.stream().map(r -> String.valueOf(r.getId())).collect(Collectors.toList());
        
        List<AmountOrder> amountOrders = amountOrderMapper.selectList(
                Wrappers.lambdaQuery(AmountOrder.class)
                        .in(AmountOrder::getOrderId, orderIdStrs)
                        .eq(AmountOrder::getType, OrderTypeEnum.PURCHASE_RETURN.getValue())
        );
        Map<String, AmountOrder> amountOrderMap = amountOrders.stream()
                .collect(Collectors.toMap(AmountOrder::getOrderId, ao -> ao, (a, b) -> a));

        // 转换VO
        List<PurchaseReturnVO> voList = records.stream().map(record -> {
            PurchaseReturnVO vo = new PurchaseReturnVO();
            BeanUtils.copyProperties(record, vo);
            AmountOrder ao = amountOrderMap.get(String.valueOf(record.getId()));
            if (ao != null) {
                vo.setAmountOrderStatus(ao.getStatus());
                vo.setAmountOrderId(ao.getId());
            }
            return vo;
        }).collect(Collectors.toList());

        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPurchaseReturn(PurchaseReturnCreateDTO createDTO, HttpServletRequest request) {
        ThrowUtils.throwIf(createDTO == null, ErrorCode.PARAMS_ERROR);
        Long productId = createDTO.getProductId();
        Long warehouseId = createDTO.getWarehouseId();
        Integer returnQuantity = createDTO.getReturnQuantity();
        if (returnQuantity <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "采退数量必须大于0");
        }
        List<InventoryDetail> detailList = inventoryDetailMapper.selectList(
                Wrappers.lambdaQuery(InventoryDetail.class)
                        .eq(InventoryDetail::getProductId, productId)
                        .eq(InventoryDetail::getWarehouseId, warehouseId)
        );
        Map<String, Integer> stockMap = calculateQuantityByProductStore(detailList);
        String key = String.format("%d_%d", productId, warehouseId);
        Integer currentStock = stockMap.getOrDefault(key, 0);
        if (returnQuantity > currentStock) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    String.format("采退数量超出库存范围，当前仓库该商品库存：%d，请求退单数量：%d", currentStock, returnQuantity));
        }
        // 查询最后一次采购表
        PurchaseOrder purchaseOrder = purchaseOrderMapper.selectOne(
                Wrappers.lambdaQuery(PurchaseOrder.class)
                        .eq(PurchaseOrder::getProductId, productId)
                        .eq(PurchaseOrder::getStatus, PurchaseOrderStatusEnum.STORED.getValue())
                        .last("LIMIT 1")
        );
        // 6. 创建采退订单
        BigDecimal totalAmount = purchaseOrder.getProductPrice().multiply(new BigDecimal(returnQuantity));
        PurchaseReturn returnOrder = new PurchaseReturn();
        returnOrder.setProductId(productId);
        returnOrder.setWarehouseId(warehouseId);
        returnOrder.setProductName(purchaseOrder.getProductName());
        returnOrder.setProductDescription(purchaseOrder.getProductDescription());
        returnOrder.setProductUrl(purchaseOrder.getProductUrl());
        returnOrder.setProductPrice(purchaseOrder.getProductPrice());
        returnOrder.setProductQuantity(returnQuantity);
        returnOrder.setStoreId(purchaseOrder.getStoreId());
        returnOrder.setSupplierId(purchaseOrder.getSupplierId());
        returnOrder.setTotalAmount(totalAmount);
        returnOrder.setStatus(PurchaseReturnStatusEnum.UNFINISHED.getValue());
        boolean save = this.save(returnOrder);
        ThrowUtils.throwIf(!save, ErrorCode.SYSTEM_ERROR, "采退表信息保存失败");

        // 7. 创建金额订单
        User loginUser = getLoginUserUtil.getLoginUser(request);
        AmountOrder amountOrder = new AmountOrder();
        amountOrder.setOrderId(String.valueOf(returnOrder.getId()));
        amountOrder.setType(OrderTypeEnum.PURCHASE_RETURN.getValue());
        amountOrder.setPayerId(purchaseOrder.getSupplierId()); // 供应商为付款人
        amountOrder.setPayeeId(loginUser.getId()); // 当前操作为收款人
        amountOrder.setAmount(totalAmount);
        amountOrder.setStoreId(purchaseOrder.getStoreId());
        amountOrder.setPayType(String.valueOf(PayTypeEnum.ALIPAY.getValue()));
        amountOrder.setStatus(PayStatusEnum.PENDING_PAYMENT.getValue());
        amountOrder.setCreateBy(loginUser.getId());
        boolean saveAmountOrder = amountOrderMapper.insert(amountOrder) > 0;
        if (!saveAmountOrder) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "创建金额订单失败");
        }
        return returnOrder.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmPurchaseReturn(Long returnId, HttpServletRequest request) {
        // 查询采退订单详情
        PurchaseReturn purchaseReturn = this.getById(returnId);
        if (purchaseReturn == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "采退订单不存在");
        }
        if (purchaseReturn.getStatus() != PurchaseReturnStatusEnum.UNFINISHED.getValue()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该采退订单已确认或已取消，无需重复操作");
        }
        Long productId = purchaseReturn.getProductId();
        Long warehouseId = purchaseReturn.getWarehouseId();
        List<InventoryDetail> detailList = inventoryDetailMapper.selectList(
                Wrappers.lambdaQuery(InventoryDetail.class)
                        .eq(InventoryDetail::getProductId, productId)
                        .eq(InventoryDetail::getWarehouseId, warehouseId)
        );
        Map<String, Integer> stockMap = calculateQuantityByProductStore(detailList);
        String key = String.format("%d_%d", productId, warehouseId);
        Integer currentStock = stockMap.getOrDefault(key, 0);
        if (purchaseReturn.getProductQuantity() > currentStock) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"当前仓库库存数量不足，无法退货");
        }
        // 校验金额订单支付状态（供应商是否已支付）
        AmountOrder amountOrder = amountOrderMapper.selectOne(
                Wrappers.lambdaQuery(AmountOrder.class)
                        .eq(AmountOrder::getOrderId, String.valueOf(returnId))
                        .eq(AmountOrder::getType, OrderTypeEnum.PURCHASE_RETURN.getValue()) // 采退金额订单类型
        );
        if (amountOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "采退金额订单不存在");
        }
        if (amountOrder.getStatus() != PayStatusEnum.PAID.getValue()) { // 假设PAID=1表示已支付
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "供应商尚未支付采退金额，无法确认退货");
        }
        // 登录用户
        User loginUser = getLoginUserUtil.getLoginUser(request);
        // 生成库存明细（标注采退类型）
        InventoryDetail inventoryDetail = new InventoryDetail();
        inventoryDetail.setProductId(purchaseReturn.getProductId());
        inventoryDetail.setOrderId(returnId); // 关联采退订单ID
        inventoryDetail.setType(InventoryDetailTypeEnum.TAKEOUT.getValue()); // 出库（采退商品出库）
        inventoryDetail.setOrderType(OrderTypeEnum.PURCHASE_RETURN.getValue()); // 采退类型（需在枚举中添加）
        inventoryDetail.setProductQuantity(purchaseReturn.getProductQuantity()); // 采退数量
        inventoryDetail.setWarehouseId(purchaseReturn.getWarehouseId()); // 退货仓库
        inventoryDetail.setCreateBy(loginUser.getId()); // 操作人（店长ID）
        boolean save = inventoryDetailMapper.insert(inventoryDetail) > 0;
        ThrowUtils.throwIf(!save,ErrorCode.SYSTEM_ERROR,"库存明细表更新失败");
        // 更新采退订单状态为已确认
        purchaseReturn.setStatus(PurchaseReturnStatusEnum.COMPLETED.getValue());
        boolean b = this.updateById(purchaseReturn);
        ThrowUtils.throwIf(!b,ErrorCode.SYSTEM_ERROR,"采退表状态更新失败");
    }

    private Map<String, Integer> calculateQuantityByProductStore(List<InventoryDetail> detailList) {
        if (CollectionUtils.isEmpty(detailList)) {
            return Collections.emptyMap();
        }
        return detailList.stream()
                .collect(Collectors.groupingBy(
                        // 分组key：商品ID_仓库ID
                        detail -> String.format("%d_%d",
                                detail.getProductId(), detail.getWarehouseId()),
                        Collectors.summingInt(detail -> {
                            Integer quantity = detail.getProductQuantity();
                            return switch (detail.getOrderType()) {
                                case 0 -> quantity;    // 采购：+数量
                                case 1 -> -quantity;   // 采退：-数量
                                case 2 -> -quantity;   // 销售：-数量
                                case 3 -> quantity;    // 销退：+数量
                                case 4 -> -quantity;   // 调拨转出：-数量（从源仓库减少）
                                case 5 -> quantity;    // 调拨转入：+数量（向目标仓库增加）
                                default -> 0;
                            };
                        })
                ));
    }
}