package com.qzh.backend.model.vo;

import com.qzh.backend.model.entity.PurchaseReturn;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

/**
 * 采退订单VO
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PurchaseReturnVO extends PurchaseReturn implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 金额订单状态（0-待支付，1-已支付，2-已取消）
     */
    private Integer amountOrderStatus;

    /**
     * 金额订单ID
     */
    private Long amountOrderId;
}
