package com.diligrp.xtrade.upay.sentinel.util;

/**
 * 常量列表
 */
public final class Constants {
    public static final String CHAR_COLON = ":";
    public static final String YYYYMM = "yyyyMM";
    // 一天的秒数
    public static final int ONE_DAY_SECONDS = 3600 * 24;
    // 提现风控设置前缀
    public static final String SENTINEL_WITHDRAW_PREFIX = "upay:sentinel:withdraw:";
    // 提现风控日提现金额
    public static final String SENTINEL_WITHDRAW_DAILYAMOUNT = "dailyAmount";
    // 提现风控日提现次数
    public static final String SENTINEL_WITHDRAW_DAILYTIMES = "dailyTimes";
    // 提现风控月提现金额
    public static final String SENTINEL_WITHDRAW_MONTHLYAMOUNT = "monthlyAmount";

    // 交易风控设置前缀
    public static final String SENTINEL_TRADE_PREFIX = "upay:sentinel:trade:";
    // 交易风控日交易金额
    public static final String SENTINEL_TRADE_DAILYAMOUNT = "dailyAmount";
    // 交易风控日交易次数
    public static final String SENTINEL_TRADE_DAILYTIMES = "dailyTimes";
    // 交易风控月交易金额
    public static final String SENTINEL_TRADE_MONTHLYAMOUNT = "monthlyAmount";
}
