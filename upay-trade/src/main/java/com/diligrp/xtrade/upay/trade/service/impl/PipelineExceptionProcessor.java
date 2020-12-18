package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.pipeline.dao.IPipelinePaymentDao;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineRequest;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineResponse;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.model.TradePayment;
import com.diligrp.xtrade.upay.trade.service.IPipelineExceptionProcessor;
import com.diligrp.xtrade.upay.trade.type.PaymentState;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 通道支付回调处理服务
 *
 * @author: brenthuang
 * @date: 2020/12/15
 */
@Service("pipelineExceptionProcessor")
public class PipelineExceptionProcessor extends PipelinePaymentProcessor implements IPipelineExceptionProcessor {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IPipelinePaymentDao pipelinePaymentDao;

    @Resource
    private IFundAccountService fundAccountService;

    /**
     * {@inheritDoc}
     *
     * 成功连接远程通道服务时回调
     * 异常处理流程: 当前阶段查询原来的交易单, 为下阶段做数据准备
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void connectSuccess(PipelineRequest request) {
        PipelinePayment pipelinePayment = request.getObject(PipelinePayment.class);

        Optional<TradePayment> paymentOpt = tradePaymentDao.findTradePaymentById(request.getPaymentId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (!PaymentState.PROCESSING.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "支付申请状态异常");
        }
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findTradeOrderById(payment.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付订单不存在"));
        if (!TradeState.FROZEN.equalTo(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "支付订单状态异常");
        }
        Optional<FrozenOrder> frozenOrderOpt = frozenOrderDao.findFrozenOrderByPaymentId(request.getPaymentId());
        FrozenOrder frozenOrder = frozenOrderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "无资金冻结记录"));
        UserAccount account = fundAccountService.findUserAccountById(payment.getAccountId());

        request.attach(pipelinePayment).attach(account).attach(payment).attach(trade).attach(frozenOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void pipelineSuccess(PipelineRequest request, PipelineResponse response) {
        super.pipelineSuccess(request, response);
        //TODO: 通知调用方处理结果
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean incPipelineTryCount(String paymentId) {
        return pipelinePaymentDao.incPipelineTryCount(paymentId) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PipelinePayment> findPipelinePayment(String paymentId) {
        return pipelinePaymentDao.findPipelinePayment(paymentId);
    }
}
