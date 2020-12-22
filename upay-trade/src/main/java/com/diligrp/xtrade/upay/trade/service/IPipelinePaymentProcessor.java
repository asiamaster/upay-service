package com.diligrp.xtrade.upay.trade.service;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineStatementQuery;
import com.diligrp.xtrade.upay.pipeline.domain.UserPipelineStatement;

/**
 * 通道支付回调处理服务接口
 *
 * @author: brenthuang
 * @date: 2020/12/12
 */
public interface IPipelinePaymentProcessor extends IPipeline.Callback {
    /**
     * 分页查询通道支付流水记录
     */
    PageMessage<UserPipelineStatement> listPipelineStatements(PipelineStatementQuery query);
}
