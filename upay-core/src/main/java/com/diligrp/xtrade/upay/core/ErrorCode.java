package com.diligrp.xtrade.upay.core;

/**
 * 系统错误码列表
 */
public class ErrorCode {
    // 系统未知异常
    public static final int SYSTEM_UNKNOWN_ERROR = 500000;
    // 无效参数错误
    public static final int ILLEGAL_ARGUMENT_ERROR = 500001;
    // 操作不允许
    public static final int OPERATION_NOT_ALLOWED = 500002;
    // 数据并发修改
    public static final int DATA_CONCURRENT_UPDATED = 500003;
    // 对象不存在
    public static final int OBJECT_NOT_FOUND = 500004;
    // 对象已存在
    public static final int OBJECT_ALREADY_EXISTS = 500005;
    // 服务不存在
    public static final int SERVICE_NOT_AVAILABLE = 501001;
    // 访问未授权
    public static final int UNAUTHORIZED_ACCESS_ERROR = 501002;
    // 交易不支持
    public static final int TRADE_NOT_SUPPORTED = 502001;
    // 不支持的支付渠道
    public static final int CHANNEL_NOT_SUPPORTED = 502002;
    // 交易不存在
    public static final int TRADE_NOT_FOUND = 502003;
    // 无效的交易状态
    public static final int INVALID_TRADE_STATE = 502004;
    // 资金账号不存在
    public static final int ACCOUNT_NOT_FOUND = 503001;
    // 无效资金账号状态
    public static final int INVALID_ACCOUNT_STATE = 503002;
    // 账号密码不正确
    public static final int INVALID_ACCOUNT_PASSWORD = 503002;
    // 账户余额不足
    public static final int INSUFFICIENT_ACCOUNT_FUND = 503003;
}
