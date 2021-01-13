package com.diligrp.xtrade.upay.trade.service;

import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 通道支付异常流程回调处理接口
 *
 * @author: brenthuang
 * @date: 2020/12/12
 */
public interface IPipelineExceptionProcessor extends IPipeline.Callback {
    /**
     * 更新异常重试次数
     */
    boolean incPipelineTryCount(String paymentId, LocalDateTime when);

    /**
     * 根据支付ID查询通道支付申请
     *
     * @param paymentId - 支付ID
     * @return 通道支付申请
     */
    Optional<PipelinePayment> findPipelinePayment(String paymentId);
}
