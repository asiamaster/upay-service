package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.util.JsonUtils;
import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.pipeline.dao.IPipelinePaymentDao;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineRequest;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineResponse;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineTransactionStatus;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.model.TradePayment;
import com.diligrp.xtrade.upay.trade.service.IPipelineExceptionProcessor;
import com.diligrp.xtrade.upay.trade.type.PaymentState;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import com.diligrp.xtrade.upay.trade.util.Constants;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
        // 异步通知业务系统处理结果
        if (response.getState() != ProcessState.PROCESSING) {
            TradeOrder trade = request.getObject(TradeOrder.class);
            PipelineTransactionStatus status = new PipelineTransactionStatus(response.getState().getCode(),
                response.getMessage(), trade.getSerialNo(), response.getStatus());
            String callbackJson = JsonUtils.toJsonString(status);
            try {
                MessageProperties properties = new MessageProperties();
                properties.setContentEncoding(Constants.CHARSET_UTF8);
                properties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
                LOG.info("Making pipeline callback request for {}", request.getPaymentId());
                Message message = new Message(callbackJson.getBytes(Constants.CHARSET_UTF8), properties);
                rabbitTemplate.send(Constants.PIPELINE_CALLBACK_EXCHANGE, Constants.PIPELINE_CALLBACK_KEY, message);
            } catch (Exception ex) {
                LOG.error(String.format("Failed to make pipeline callback request for %s", request.getPaymentId()), ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean incPipelineTryCount(String paymentId, LocalDateTime when) {
        Map<String, Object> params = new HashMap<>(2);
        params.put("paymentId", paymentId);
        params.put("when", when);
        return pipelinePaymentDao.incPipelineTryCount(params) > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<PipelinePayment> findPipelinePayment(String paymentId) {
        return pipelinePaymentDao.findPipelinePayment(paymentId);
    }
}
