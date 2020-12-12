package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.dao.IUserStatementDao;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.FrozenStateDto;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.channel.model.UserStatement;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.FrozenState;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.channel.type.StatementType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.type.AccountType;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.AsyncTaskExecutor;
import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineRequest;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineResponse;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;
import com.diligrp.xtrade.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentStateDto;
import com.diligrp.xtrade.upay.trade.domain.TradeStateDto;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.PaymentFee;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.model.TradePayment;
import com.diligrp.xtrade.upay.trade.service.IBankWithdrawPipelineProcessor;
import com.diligrp.xtrade.upay.trade.type.FundType;
import com.diligrp.xtrade.upay.trade.type.PaymentState;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 银行圈提通道回调处理服务
 *
 * @author: brenthuang
 * @date: 2020/12/12
 */
@Service("bankWithdrawPipelineProcessor")
public class BankWithdrawPipelineProcessor implements IBankWithdrawPipelineProcessor {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IUserStatementDao userStatementDao;

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void pipelineConnected(PipelineRequest request, PipelineResponse response) {
        TradeOrder trade = request.getObject(TradeOrder.class);
        Payment payment = request.getObject(Payment.class);
        UserAccount account = request.getObject(UserAccount.class);
        LocalDateTime now = LocalDateTime.now().withNano(0);

        // 生成"处理中"的支付记录
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        // 发送通道请求前冻结资金
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
        transaction.freeze(payment.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId()).name(trade.getName())
            .cardNo(null).amount(payment.getAmount()).fee(0L).state(PaymentState.PROCESSING.getCode())
            .description(null).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);
        // 创建冻结资金订单
        Long masterAccountId = account.getParentId() == 0 ? account.getAccountId() : account.getParentId();
        Long childAccountId = account.getParentId() == 0 ? null : account.getAccountId();
        IKeyGenerator frozenKey = keyGeneratorManager.getKeyGenerator(SequenceKey.FROZEN_ID);
        // 异步执行避免Seata回滚造成ID重复
        long frozenId = AsyncTaskExecutor.submit(() -> frozenKey.nextId());
        FrozenOrder frozenOrder = FrozenOrder.builder().frozenId(frozenId).paymentId(paymentId)
            .accountId(masterAccountId).childId(childAccountId).name(account.getName())
            .type(FrozenType.TRADE_FROZEN.getCode()).amount(payment.getAmount()).state(FrozenState.FROZEN.getCode())
            .description(null).version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);
        request.attach(paymentDo).attach(frozenOrder);
        response.setPaymentId(paymentId);
        response.setStatus(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void pipelineSuccess(PipelineRequest request, PipelineResponse response) {
        TradeOrder trade = request.getObject(TradeOrder.class);
        TradePayment payment = request.getObject(TradePayment.class);
        UserAccount account = request.getObject(UserAccount.class);
        FrozenOrder frozenOrder = request.getObject(FrozenOrder.class);
        String paymentId = payment.getPaymentId();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        TransactionStatus status = null;
        // 通道处理成功则解冻并扣减资金
        if (response.getState() == ProcessState.SUCCESS) {
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            transaction.unfreeze(payment.getAmount());
            transaction.outgo(payment.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
            if (response.fee() > 0) {
                transaction.outgo(response.fee(), FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName());
            }
            status = accountChannelService.submit(transaction);
            // 修改冻结订单"已解冻"状态
            FrozenStateDto frozenState = FrozenStateDto.of(frozenOrder.getFrozenId(), FrozenState.UNFROZEN.getCode(),
                frozenOrder.getVersion(), now);
            if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            // 更新支付订单状态
            if (response.fee() > 0) {
                paymentFeeDao.insertPaymentFee(PaymentFee.of(paymentId, response.fee(),
                    FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName(), now));
            }
            PaymentStateDto paymentState = PaymentStateDto.of(paymentId, null, response.getFee(),
                PaymentState.SUCCESS.getCode(), payment.getVersion(), now);
            if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, null,
                TradeState.SUCCESS.getCode(), trade.getVersion(), now);
            if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            // 计算实际操作金额, 生成交易冲正时账户业务账单
            UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId)
                .channelId(payment.getChannelId()).accountId(payment.getAccountId(), account.getParentId())
                .type(StatementType.WITHDRAW.getCode()).typeName(StatementType.WITHDRAW.getName())
                .amount(-payment.getAmount() - response.fee()).fee(response.fee())
                .balance(status.getBalance() + status.getAmount())
                .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount())
                .serialNo(trade.getSerialNo()).state(4).createdTime(now).build();
            userStatementDao.insertUserStatement(statement);

            // 处理商户收款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
            // 如果是商户账户圈提, 圈提手续费商户自行承担, 不能作为园区收益
            if (response.fee() > 0 && !AccountType.MERCHANT.equalTo(account.getType())) {
                MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
                AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
                IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
                feeTransaction.income(response.fee(), FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName());
                accountChannelService.submitExclusively(feeTransaction);
            }
        } else if (response.getState() == ProcessState.FAILED) {
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            transaction.unfreeze(payment.getAmount());
            status = accountChannelService.submitOnce(transaction);
            // 修改冻结订单"已解冻"状态
            FrozenStateDto frozenState = FrozenStateDto.of(frozenOrder.getFrozenId(), FrozenState.UNFROZEN.getCode(),
                frozenOrder.getVersion(), now);
            if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, null,
                TradeState.CLOSED.getCode(), trade.getVersion(), now);
            if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            PaymentStateDto paymentState = PaymentStateDto.of(paymentId, null, response.getFee(),
                PaymentState.FAILED.getCode(), payment.getVersion(), now);
            if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
        }
        response.setStatus(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void pipelineTimeout(PipelineRequest request) {
        //TODO: 异常处理策略
    }
}
