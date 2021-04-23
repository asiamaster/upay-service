package com.diligrp.xtrade.upay.trade.util;

/**
 * 交易模块常量列表
 *
 * @author: brenthuang
 * @date: 2020/12/16
 */
public class Constants {
    // 支付通道异常重试死信队列
    public static final String PIPELINE_RECOVER_QUEUE = "pipelineRecoverQueue";
    // 支付通道异常重试死信队列交换机
    public static final String PIPELINE_RECOVER_EXCHANGE = "pipeline.recover.exchange";
    // 支付通道异常重试死信队列路由KEY
    public static final String PIPELINE_RECOVER_KEY = "pipeline.recover.key";
    // 支付通道异常处理队列
    public static final String PIPELINE_EXCEPTION_QUEUE = "pipelineExceptionQueue";
    // 支付通道异常处理交换机
    public static final String PIPELINE_EXCEPTION_EXCHANGE = "pipeline.exception.exchange";
    // 支付通道异常处理路由KEY
    public static final String PIPELINE_EXCEPTION_KEY = "pipeline.exception.key";
    // 支付通道异步回调处理队列 - 通知业务系统处理结果
    public static final String PIPELINE_CALLBACK_QUEUE = "pipelineCallbackQueue";
    // 支付通道异步回调处理交换机 - 通知业务系统处理结果
    public static final String PIPELINE_CALLBACK_EXCHANGE = "pipeline.callback.exchange";
    // 支付通道异步回调处理路由KEY - 通知业务系统处理结果
    public static final String PIPELINE_CALLBACK_KEY = "pipeline.callback.key";
    // 死信队列最小消息过期时间(毫秒) - 1分钟
    public static final long MIN_MESSAGE_DELAY_TIME = 1 * 60 * 1000;
    // 死信队列最大消息过期时间(毫秒) - 2小时
    public static final long MAX_MESSAGE_DELAY_TIME = 2 * 60 * 60 * 1000;
    // 最大异常重试次数
    public static final long MAX_EXCEPTION_RETRY_TIMES = 5;
    // 5分钟
    public static final long FIVE_MINUTES_IN_MILLIS = 5 * 60 * 1000;

    public final static String CHARSET_UTF8 = "utf-8";
}
