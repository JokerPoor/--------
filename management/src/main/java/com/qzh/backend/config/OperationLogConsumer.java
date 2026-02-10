package com.qzh.backend.config;

import com.qzh.backend.model.dto.operationLog.OperationLogMessage;
import com.qzh.backend.model.entity.OperationLog;
import com.qzh.backend.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 操作日志消费者
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OperationLogConsumer {

    private final OperationLogService operationLogService;

    /**
     * 监听日志队列，消费消息并保存日志
     */
    @RabbitListener(queues = RabbitMQConfig.OPERATION_LOG_QUEUE)
    public void consumeOperationLog(OperationLogMessage logMessage) {
        try {
            if (logMessage == null) {
                log.warn("消费操作日志消息失败：消息为空");
                return;
            }
            // 转换为实体类并保存
            OperationLog operationLog = new OperationLog();
            operationLog.setOperatorId(logMessage.getOperatorId());
            operationLog.setOperatorDevice(logMessage.getOperatorDevice());
            operationLog.setOperatorIp(logMessage.getOperatorIp());
            operationLog.setOperationTime(logMessage.getOperationTime());
            operationLog.setSystemModule(logMessage.getSystemModule());
            operationLog.setOperationContent(logMessage.getOperationContent());
            operationLog.setOperationResult(logMessage.getOperationResult());
            operationLog.setErrorMsg(logMessage.getErrorMsg());
            
            operationLogService.save(operationLog);
            log.info("成功消费并保存操作日志，操作人ID：{}", logMessage.getOperatorId());
        } catch (Exception e) {
            log.error("消费操作日志消息失败", e);
            // 可根据业务需求添加重试机制
        }
    }
}