package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.service.IChannelRouteService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 渠道路由服务实现
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
@Service("channelRouteService")
public class ChannelRouteServiceImpl implements IChannelRouteService {

    @Resource
    private IPaymentPipelineManager paymentPipelineManager;

    /**
     * {@inheritDoc}
     *
     * 当前业务需求不需进行路由选择，直接使用默认路由即可
     */
    @Override
    public IPipeline selectPaymentPipeline(long mchId, int channelId, long amount) {
        ChannelType.getType(channelId).orElseThrow(() ->
            new PaymentChannelException(ErrorCode.CHANNEL_NOT_SUPPORTED, "不支持该支付渠道"));
        List<IPipeline> pipelines = paymentPipelineManager.supportedPipelines().stream()
            .filter(p -> p.mchId() == mchId).collect(Collectors.toList());
        if (pipelines.isEmpty()) {
            throw new PaymentChannelException(ErrorCode.OBJECT_NOT_FOUND, "该商户未配置任何支付通道");
        }
        if (pipelines.size() > 1) {
            throw new PaymentChannelException(ErrorCode.OBJECT_NOT_FOUND, "该商户未配置支付路由");
        }
        return pipelines.get(0);
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
