package com.qzh.backend.model.dto.operationLog;

import lombok.Data;

import java.util.Date;

/**
 * 操作日志消息DTO
 */
@Data
public class OperationLogMessage {
    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作设备（设备/系统/浏览器）
     */
    private String operatorDevice;

    /**
     * 操作IP
     */
    private String operatorIp;

    /**
     * 操作时间
     */
    private Date operationTime;

    /**
     * 系统模块
     */
    private String systemModule;

    /**
     * 操作内容
     */
    private String operationContent;

    /**
     * 操作结果（成功/失败）
     */
    private String operationResult;

    /**
     * 错误信息（失败时填充）
     */
    private String errorMsg;
}