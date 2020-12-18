package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;

/**
 * 渠道路由服务接口
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
public interface IChannelRouteService {

    /**
     * 根据策略选择最优支付通道
     *
     * @param tradeType - 交易类型
     * @param channelId - 渠道ID
     * @param amount - 交易金额
     * @return 支付通道
     */
    IPipeline selectPaymentPipeline(int tradeType, int channelId, long amount);

    /**
     * 根据支付通道编码查询支付通道
     *
     * @param code - 通道编码
     * @return 支付通道
     */
    IPipeline findPaymentPipeline(String code);
}
