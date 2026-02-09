package com.qzh.backend.model.dto.product;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qzh.backend.common.PageRequest;
import com.qzh.backend.model.entity.PurchaseReturn;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class PurchaseReturnQueryDTO extends PageRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 门店ID */
    private Long storeId;

    /** 订单状态 */
    private Integer status;

    public static QueryWrapper<PurchaseReturn> getQueryWrapper(PurchaseReturnQueryDTO  dto) {
        QueryWrapper<PurchaseReturn> queryWrapper = new QueryWrapper<>();
        if(dto == null){
            return queryWrapper;
        }
        Long productId = dto.getProductId();
        String productName = dto.getProductName();
        Long storeId = dto.getStoreId();
        Integer status = dto.getStatus();
        String sortField = dto.getSortField();
        String sortOrder = dto.getSortOrder();
        
        queryWrapper.eq(ObjectUtil.isNotNull(productId),"productId", productId);
        queryWrapper.like(ObjectUtil.isNotNull(productName),"productName", productName);
        queryWrapper.eq(ObjectUtil.isNotNull(storeId),"storeId", storeId);
        queryWrapper.eq(ObjectUtil.isNotNull(status),"status", status);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }
}
