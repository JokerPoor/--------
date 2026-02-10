package com.qzh.backend.model.dto.transfer;

import com.qzh.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(callSuper = true)
@Data
public class TransferLogQueryDTO extends PageRequest implements Serializable {

    private Long id;

    private Long transferOrderId;

    private Long sourceWarehouseId;

    private Long targetWarehouseId;

    private Long productId;

    private String remark;

    private Date startTime;

    private Date endTime;

    private static final long serialVersionUID = 1L;
}
