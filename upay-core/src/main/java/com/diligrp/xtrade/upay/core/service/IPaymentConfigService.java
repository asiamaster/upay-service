package com.diligrp.xtrade.upay.core.service;

/**
 * 支付服务参数配置服务接口
 */
public interface IPaymentConfigService {
    /**
     * 接口数据签名是否开启
     *
     * @param groupCode - 必填, 字典分组编码(市场编码)
     * @return 是否开启数据签名
     */
    boolean dataSignSwitch(String groupCode);

    /**
     * 是否开启短信通知
     *
     * @param groupCode - 必填, 字典分组编码(市场编码)
     * @return 是否开启短信通知 - 字典分组编码(市场编码)
     */
    boolean smsNotifySwitch(String groupCode);

    /**
     * 最大免密协议支付金额
     *
     * @param groupCode - 必填, 字典分组编码(市场编码)
     * @return 最大免密协议支付金额
     */
    Long maxProtocolAmount(String groupCode);

    /**
     * 获取数据字典配置值
     *
     * @param code - 必填, 参数编码
     * @return 参数配置值
     */
    String loadSystemGlobalConf(String code);
}