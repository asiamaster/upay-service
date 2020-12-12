package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;

/**
 * 通道路由领域模型
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
public class RoutePass {
    // 支付渠道
    private ChannelType channel;
    // 支付通道
    private PipelineType pipeline;

    public static RoutePass of(ChannelType channel, PipelineType pipeline) {
        RoutePass route = new RoutePass();
        route.setChannel(channel);
        route.setPipeline(pipeline);
        return route;
    }

    public ChannelType getChannel() {
        return channel;
    }

    public void setChannel(ChannelType channel) {
        this.channel = channel;
    }

    public PipelineType getPipeline() {
        return pipeline;
    }

    public void setPipeline(PipelineType pipeline) {
        this.pipeline = pipeline;
    }
}
