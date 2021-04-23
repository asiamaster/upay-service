package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.dao.IUserStatementDao;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.model.UserStatement;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.channel.type.StatementType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.DataPartition;
import com.diligrp.xtrade.upay.trade.dao.IRefundPaymentDao;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.domain.PaymentStateDto;
import com.diligrp.xtrade.upay.trade.domain.Refund;
import com.diligrp.xtrade.upay.trade.domain.TradeStateDto;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.PaymentFee;
import com.diligrp.xtrade.upay.trade.model.RefundPayment;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.model.TradePayment;
import com.diligrp.xtrade.upay.trade.service.IPaymentService;
import com.diligrp.xtrade.upay.trade.type.FundType;
import com.diligrp.xtrade.upay.trade.type.PaymentState;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import com.diligrp.xtrade.upay.trade.type.TradeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 账户转账业务：转入方与转出方进行资金转账，暂不支持收取费用
 */
@Service("transferPaymentService")
public class TransferPaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IRefundPaymentDao refundPaymentDao;

    @Resource
    private IUserStatementDao userStatementDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     * 转账只支持账户/余额渠道，且不支持费用收取
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forTrade(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行转账业务");
        }
        if (trade.getAccountId().equals(payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "同一账号不能进行交易");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        feesOpt.ifPresent(fees -> { throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "转账暂不支持收取费用"); });

        // 交易转出
        LocalDateTime now = LocalDateTime.now().withNano(0);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        UserAccount fromAccount = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), maxPwdErrors);
        UserAccount toAccount = fundAccountService.findUserAccountById(trade.getAccountId());
        if (!ObjectUtils.equals(fromAccount.getMchId(), toAccount.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能进行跨商户交易");
        }
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑

        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel fromChannel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(trade.getType(), now);
        fromTransaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 交易转入
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        AccountChannel toChannel = AccountChannel.of(paymentId, toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(trade.getType(), now);
        toTransaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        status.setRelation(accountChannelService.submit(toTransaction));

        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.SUCCESS.getCode(),
            trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }

        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(payment.getAccountId()).name(fromAccount.getName())
            .cardNo(null).amount(payment.getAmount()).fee(0L).state(PaymentState.SUCCESS.getCode())
            .description(TradeType.TRANSFER.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        // 生成转账双方的业务账单
        List<UserStatement> statements = new ArrayList<>(2);
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
            .channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId(), fromAccount.getParentId())
            .type(StatementType.TRANSFER.getCode()).typeName(StatementType.TRANSFER.getName())
            .amount(- paymentDo.getAmount()).fee(0L).balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).collect(statements);
        TransactionStatus relation = status.getRelation();
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
            .channelId(paymentDo.getChannelId()).accountId(trade.getAccountId(), toAccount.getParentId())
            .type(StatementType.TRANSFER.getCode()).typeName(StatementType.TRANSFER.getName())
            .amount(paymentDo.getAmount()).fee(0L).balance(relation.getBalance() + relation.getAmount())
            .frozenAmount(relation.getFrozenBalance() + relation.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).collect(statements);
        userStatementDao.insertUserStatements(DataPartition.strategy(fromAccount.getMchId()), statements);
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * 撤销转账-转账金额逆向操作，交易撤销需要修改交易订单状态
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        if (trade.getState() != TradeState.SUCCESS.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效的交易状态，不能进行撤销操作");
        }

        // "转账"业务不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));

        // 撤销交易，需验证退款方账户状态无须验证密码
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UserAccount fromAccount = accountChannelService.checkTradePermission(trade.getAccountId());
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());

        // 处理转入方退款
        AccountChannel fromChannel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
        fromTransaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 处理转出方收款
        UserAccount toAccount = fundAccountService.findUserAccountById(payment.getAccountId());
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        AccountChannel toChannel = AccountChannel.of(paymentId, toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
        toTransaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        status.setRelation(accountChannelService.submit(toTransaction));

        RefundPayment refund = RefundPayment.builder().paymentId(paymentId).type(TradeType.CANCEL_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).amount(trade.getAmount()).fee(0L)
            .state(TradeState.SUCCESS.getCode()).description(null).version(0).createdTime(now).build();
        refundPaymentDao.insertRefundPayment(refund);
        // 撤销支付记录
        PaymentStateDto paymentState = PaymentStateDto.of(payment.getPaymentId(), PaymentState.CANCELED.getCode(),
            payment.getVersion(), now);
        if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // 撤销交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.CANCELED.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }

        // 生成交易双方的业务账单
        List<UserStatement> statements = new ArrayList<>(2);
        TransactionStatus relation = status.getRelation();
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId).channelId(payment.getChannelId())
            .accountId(payment.getAccountId(), toAccount.getParentId()).type(StatementType.REFUND.getCode())
            .typeName(StatementType.TRANSFER.getName() + "-" +StatementType.REFUND.getName())
            .amount(payment.getAmount() + payment.getFee()).fee(payment.getFee())
            .balance(relation.getBalance() + relation.getAmount())
            .frozenAmount(relation.getFrozenBalance() + relation.getFrozenAmount())
            .serialNo(trade.getSerialNo()).state(4).createdTime(now).collect(statements);
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId).channelId(payment.getChannelId())
            .accountId(trade.getAccountId(), fromAccount.getParentId()).type(StatementType.REFUND.getCode())
            .typeName(StatementType.TRANSFER.getName() + "-" +StatementType.REFUND.getName())
            .amount(- payment.getAmount() + trade.getFee()).fee(trade.getFee())
            .balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount())
            .serialNo(trade.getSerialNo()).state(4).createdTime(now).collect(statements);
        userStatementDao.insertUserStatements(DataPartition.strategy(fromAccount.getMchId()), statements);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.TRANSFER;
    }
}
