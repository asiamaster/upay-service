package com.diligrp.xtrade.upay.trade;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.trade.util.Constants;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 交易模块SpringBoot集成配置
 */
@Configuration
@ComponentScan("com.diligrp.xtrade.upay.trade")
@MapperScan(basePackages =  {"com.diligrp.xtrade.upay.trade.dao"}, markerInterface = MybatisMapperSupport.class)
public class TradeConfiguration {

    /**
     * 创建支付通道异常重试死信队列
     * 队列为持久化、非独占式且不自动删除的死信队列, 利用死信队列的消息过期特性用于重试策略, 比如：重试间隔1min 5min 10min 120min
     */
    @Bean
    public Queue pipelineRecoverQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", Constants.PIPELINE_EXCEPTION_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", Constants.PIPELINE_EXCEPTION_KEY);
        return new Queue(Constants.PIPELINE_RECOVER_QUEUE, true, false, false, arguments);
    }

    /**
     * 创建支付通道异常重试交换机
     */
    @Bean
    public DirectExchange pipelineRecoverExchange() {
        return new DirectExchange(Constants.PIPELINE_RECOVER_EXCHANGE, true, false);
    }

    /**
     * 创建支付通道异常重试绑定
     */
    @Bean
    public Binding pipelineRecoverBinding() {
        return BindingBuilder.bind(pipelineRecoverQueue()).to(pipelineRecoverExchange()).with(Constants.PIPELINE_RECOVER_KEY);
    }

    /**
     * 创建支付通道异常处理队列: 支付通道处理异常或没用明确的处理结果时用于做业务补偿(主动查询处理结果补偿本地事务)
     * 队列支持持久化、非独占式且不自动删除
     */
    @Bean
    public Queue pipelineExceptionQueue() {
        return new Queue(Constants.PIPELINE_EXCEPTION_QUEUE, true, false, false);
    }

    /**
     * 创建支付通道异常处理交换机
     */
    @Bean
    public DirectExchange pipelineExceptionExchange() {
        return new DirectExchange(Constants.PIPELINE_EXCEPTION_EXCHANGE, true, false);
    }

    /**
     * 创建支付通道异常处理绑定
     */
    @Bean
    public Binding pipelineExceptionBinding() {
        return BindingBuilder.bind(pipelineExceptionQueue()).to(pipelineExceptionExchange()).with(Constants.PIPELINE_EXCEPTION_KEY);
    }

    /**
     * 创建支付通道异常处理队列: 支付通道处理异常或没用明确的处理结果时用于做业务补偿(主动查询处理结果补偿本地事务)
     * 队列支持持久化、非独占式且不自动删除
     */
    @Bean
    public Queue pipelineCallbackQueue() {
        return new Queue(Constants.PIPELINE_CALLBACK_QUEUE, true, false, false);
    }

    /**
     * 创建支付通道异常处理交换机
     */
    @Bean
    public DirectExchange pipelineCallbackExchange() {
        return new DirectExchange(Constants.PIPELINE_CALLBACK_EXCHANGE, true, false);
    }

    /**
     * 创建支付通道异常处理绑定
     */
    @Bean
    public Binding pipelineCallbackBinding() {
        return BindingBuilder.bind(pipelineCallbackQueue()).to(pipelineCallbackExchange()).with(Constants.PIPELINE_CALLBACK_KEY);
    }
}