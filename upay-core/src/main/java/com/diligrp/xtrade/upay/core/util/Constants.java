package com.diligrp.xtrade.upay.core.util;

/**
 * 常量列表
 */
public final class Constants {
    public static final String CHAR_UNDERSCORE = "_";

    public static final String CHAR_COLON = ":";

    // 买家费用
    public static final int FOR_BUYER = 1;
    // 卖家费用
    public static final int FOR_SELLER = 2;
    // 数据字典全局组名-适用所有商户的配置组，字典值分组的目的是处理各商户有独立的配置信息
    public static final String GLOBAL_CFG_GROUP = "GlobalSysCfg";
    // 数据字典常量-接口数据签名配置参数
    public static final String CONFIG_DATA_SIGN = "dataSignSwitch";
    // 数据字典常量-短信通知配置参数
    public static final String CONFIG_SMS_NOTIFY = "smsNotifySwitch";
    // 数据字典常量-最大免密支付金额
    public static final String CONFIG_MAX_PROTO_AMOUNT = "maxProtocolAmount";
    // 默认最大免密支付金额 - 100元
    public static final String DEFAULT_MAX_PROTO_AMOUNT = "10000";
    // 默认无限制密码错误次数
    public static final int DEFAULT_MAX_PASSWORD_ERRORS = -1;
    // 数据字典常量-参数值: 开关打开
    public static final String SWITCH_ON = "on";
    // 数据字典常量-参数值: 开关关闭
    public static final String SWITCH_OFF = "off";
}
