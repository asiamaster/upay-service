package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.upay.pipeline.type.PipelineType;

/**
 * 支付通道抽象模型
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
public abstract class AbstractPipeline implements IPipeline {
    // 通道类型
    protected PipelineType type;
    // 通道名称
    protected String name;
    // 通道访问URI
    protected String uri;
    // 通道参数
    protected String param;
    // 所属商户
    protected long mchId;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configPipeline(PipelineType type, String name, String uri, String param, long mchId) {
        this.type = type;
        this.name = name;
        this.uri = uri;
        this.param = param;
        this.mchId = mchId;
    }

    @Override
    public long mchId() {
        return mchId;
    }

    @Override
    public String code() {
        return type.code();
    }

    public String name() {
        return name;
    }

    @Override
    public int channelId() {
        return type.getChannelId();
    }

    public String uri() {
        return uri;
    }

    public String param() {
        return param;
    }
}
