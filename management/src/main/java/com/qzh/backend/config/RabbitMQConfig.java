package com.qzh.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 日志队列名称
    public static final String OPERATION_LOG_QUEUE = "operation.log.queue";
    // 日志交换机名称
    public static final String OPERATION_LOG_EXCHANGE = "operation.log.exchange";
    // 日志路由键
    public static final String OPERATION_LOG_ROUTING_KEY = "operation.log.key";

    /**
     * 声明日志队列
     */
    @Bean
    public Queue operationLogQueue() {
        // durable: 队列持久化
        return new Queue(OPERATION_LOG_QUEUE, true);
    }

    /**
     * 声明直连交换机
     */
    @Bean
    public DirectExchange operationLogExchange() {
        return new DirectExchange(OPERATION_LOG_EXCHANGE, true, false);
    }

    /**
     * 绑定队列和交换机
     */
    @Bean
    public Binding operationLogBinding(Queue operationLogQueue, DirectExchange operationLogExchange) {
        return BindingBuilder.bind(operationLogQueue).to(operationLogExchange).with(OPERATION_LOG_ROUTING_KEY);
    }

    /**
     * 使用JSON序列化消息
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}