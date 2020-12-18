package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.upay.channel.domain.RoutePass;
import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.service.IChannelRouteService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 渠道路由服务实现
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
@Service("channelRouteService")
public class ChannelRouteServiceImpl implements IChannelRouteService {
    // 默认路由
    private Map<ChannelType, RoutePass> routes = new ConcurrentHashMap();

    @Resource
    private IPaymentPipelineManager paymentPipelineManager;

    // 注册默认路由, 如: 盛京银行渠道默认使用盛京银行通道
    {
        registerDefaultRoute(RoutePass.of(ChannelType.SJ_BANK, PipelineType.SJ_BANK));
    }

    public void registerDefaultRoute(RoutePass route) {
        routes.put(route.getChannel(), route);
    }

    /**
     * {@inheritDoc}
     *
     * 当前业务需求不需进行路由选择，直接使用默认路由即可
     */
    @Override
    public IPipeline selectPaymentPipeline(int tradeType, int channelId, long amount) {
        ChannelType channel = ChannelType.getType(channelId).orElseThrow(
            () -> new PaymentChannelException(ErrorCode.CHANNEL_NOT_SUPPORTED, "不支持该支付渠道"));
        RoutePass route = Optional.ofNullable(routes.get(channel)).orElseThrow(
            () -> new PaymentChannelException(ErrorCode.OBJECT_NOT_FOUND, "找不到该渠道的支付路由"));
        return paymentPipelineManager.loadPipeline(route.getPipeline());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPipeline findPaymentPipeline(String code) {
        PipelineType pipelineType = PipelineType.getType(code).orElseThrow(
            () -> new PaymentChannelException(ErrorCode.PIPELINE_NOT_SUPPORTED, "不支持选择的支付通道"));
        return paymentPipelineManager.loadPipeline(pipelineType);
    }
}
