package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.upay.pipeline.type.Pipeline;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;

/**
 * 盛京银行通道领域模型
 *
 * @author: brenthuang
 * @date: 2020/12/09
 */
@Pipeline(type = PipelineType.SJ_BANK)
public class SjBankPipeline extends AbstractPipeline {

    /**
     * {@inheritDoc}
     *
     * 远程通道发起支付申请
     */
    @Override
    public PipelineResponse sendTradeRequest(PipelineRequest request, Callback callback) {
        PipelineResponse response = new PipelineResponse();
        callback.connectSuccess(request);
        // 调用第三方支付通道, 假设返回成功
        response.setState(ProcessState.PROCESSING);
        response.setSerialNo("123456");
        response.setFee(100L);
        callback.pipelineSuccess(request, response);
        return response;
    }

    /**
     * {@inheritDoc}
     *
     * 远程通道第一次失败时异常重试处理流程, 查询交易状态更新本地事务
     */
    @Override
    public PipelineResponse sendQueryRequest(PipelineRequest request, Callback callback) {
        PipelineResponse response = new PipelineResponse();
        callback.connectSuccess(request);
        // 调用第三方支付通道, 假设返回成功
        response.setState(ProcessState.SUCCESS);
        response.setSerialNo("123456");
        callback.pipelineSuccess(request, response);
        return response;
    }
}