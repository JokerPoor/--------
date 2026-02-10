package com.qzh.backend.model.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;

@Data
public class TransferLogVO implements Serializable {
    private Long id;
    private Long transferOrderId;
    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long targetWarehouseId;
    private String targetWarehouseName;
    private Long productId;
    private String productName;
    private String productUrl;
    private Integer transferQuantity;
    private String remark;
    private Date createTime;
    
    private static final long serialVersionUID = 1L;
}
