package com.qzh.backend.model.dto.message;

import lombok.Data;
/**
 * 消息分页查询DTO
 */
@Data
public class MessageQueryDTO {

    /**
     * 页码（默认1）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数（默认20）
     */
    private Integer pageSize = 20;

    /**
     * 接收人ID（必传）
     */
    private Long recipientId;

    /**
     * 消息类型（0-自动调拨,1-自动购买）
     */
    private String messageType;

    /**
     * 已读状态（0-未读,1-已读）
     */
    private Integer readStatus;
}