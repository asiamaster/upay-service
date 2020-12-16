package com.diligrp.xtrade.upay.pipeline.service;

import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;

/**
 * 支付通道管理器接口
 *
 * @author: brenthuang
 * @date: 2020/12/09
 */
public interface IPaymentPipelineManager {
    /**
     * 注册支付通道
     */
    void registerPipeline(PipelineType type, IPipeline pipeline);

    /**
     * 根据通道编码获取支付通道
     */
    IPipeline loadPipeline(PipelineType type);
}
