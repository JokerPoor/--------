package com.qzh.backend.aop;

import com.qzh.backend.annotation.LogInfoRecord;
import com.qzh.backend.config.RabbitMQConfig;
import com.qzh.backend.model.dto.operationLog.OperationLogMessage;
import com.qzh.backend.model.entity.User;
import com.qzh.backend.utils.GetLoginUserUtil;
import com.qzh.backend.utils.GetMessageUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Date;

import static com.qzh.backend.constants.OperationStatusConstant.OPERATION_STATUS_FAILURE;
import static com.qzh.backend.constants.OperationStatusConstant.OPERATION_STATUS_SUCCESS;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class

OperationLogAspect {

    private final GetLoginUserUtil getLoginUserUtil;

    private final RabbitTemplate rabbitTemplate;

    @AfterReturning("@annotation(logInfoRecord)")
    public void recordLogAfterReturning(JoinPoint joinPoint, LogInfoRecord logInfoRecord) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                log.warn("操作日志记录失败：无法获取请求上下文");
                return;
            }
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = servletRequestAttributes.getRequest();

            // 构建日志消息
            OperationLogMessage logMessage = buildOperationLogMessage(joinPoint, logInfoRecord, request, OPERATION_STATUS_SUCCESS, null);

            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(RabbitMQConfig.OPERATION_LOG_EXCHANGE,
                    RabbitMQConfig.OPERATION_LOG_ROUTING_KEY, logMessage);
        } catch (Exception e) {
            log.error("发送操作日志消息到RabbitMQ失败", e);
        }
    }

    /**
     * 异常通知：方法执行失败后发送日志消息
     */
    @AfterThrowing(value = "@annotation(logInfoRecord)",throwing = "e")
    public void recordLogAfterThrowing(JoinPoint joinPoint, LogInfoRecord logInfoRecord, Exception e) {
        try {
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                log.warn("操作日志记录失败：无法获取请求上下文");
                return;
            }
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = servletRequestAttributes.getRequest();

            // 构建日志消息（包含错误信息）
            OperationLogMessage logMessage = buildOperationLogMessage(joinPoint, logInfoRecord, request, OPERATION_STATUS_FAILURE, e.getMessage());

            // 发送到RabbitMQ
            rabbitTemplate.convertAndSend(RabbitMQConfig.OPERATION_LOG_EXCHANGE,
                    RabbitMQConfig.OPERATION_LOG_ROUTING_KEY, logMessage);
        } catch (Exception ex) {
            log.error("发送异常操作日志消息到RabbitMQ失败", ex);
        }
    }

    private OperationLogMessage buildOperationLogMessage(JoinPoint joinPoint, LogInfoRecord logInfoRecord,
                                                         HttpServletRequest request, String operationResult, String errorMsg) {
        // 用户信息
        User loginUser = getLoginUserUtil.getLoginUser(request);
        if (loginUser == null) {
            log.warn("操作日志记录失败：未获取到登录用户");
            return null;
        }
        // IP地址
        String ipAddress = GetMessageUtils.getClientIpAddress(request);
        // 设备 浏览器
        String device = GetMessageUtils.getDevice(request);
        String browser = GetMessageUtils.getBrowser(request);
        String os = GetMessageUtils.getOs(request);

        OperationLogMessage logMessage = new OperationLogMessage();
        logMessage.setOperatorId(loginUser.getId());
        logMessage.setOperatorDevice(String.format("%s/%s/%s", device, os, browser));
        logMessage.setOperatorIp(ipAddress);
        logMessage.setOperationTime(new Date());
        logMessage.setSystemModule(logInfoRecord.SystemModule());
        logMessage.setOperationContent(String.format("方法入参:%s", Arrays.toString(joinPoint.getArgs())));
        logMessage.setOperationResult(operationResult);
        logMessage.setErrorMsg(errorMsg);
        return logMessage;
    }


}
