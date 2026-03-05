package com.qzh.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 系统定时任务消息通知表实体类
 * 对应表：sys_message（字段为驼峰命名）
 */
@Data
@TableName("sys_message")
public class SysMessage implements Serializable {


    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 消息类型(0-自动调拨,1-自动购买)
     */
    private String messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联业务ID(调拨单ID/采购单ID)
     */
    private Long relatedId;

    /**
     * 关联商品ID
     */
    private Long productId;

    /**
     * 消息接收人ID
     */
    private Long recipientId;

    /**
     * 已读状态(0-未读,1-已读)
     */
    private Integer readStatus;

    /**
     * 创建时间（自动填充）
     */
    private Date createTime;

    /**
     * 已读时间
     */
    private Date readTime;

}