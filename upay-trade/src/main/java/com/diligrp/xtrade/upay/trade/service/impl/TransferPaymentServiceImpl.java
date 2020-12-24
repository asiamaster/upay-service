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
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.domain.TradeStateDto;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
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
    private IUserStatementDao userStatementDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IFundAccountService fundAccountService;

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
        UserAccount fromAccount = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), -1);
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑
        if (!ObjectUtils.equals(fromAccount.getMchId(), trade.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能进行跨商户转账");
        }
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel fromChannel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(trade.getType(), now);
        fromTransaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 交易转入
        UserAccount toAccount = fundAccountService.findUserAccountById(trade.getAccountId());
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        AccountChannel toChannel = AccountChannel.of(paymentId, toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(trade.getType(), now);
        toTransaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
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
        userStatementDao.insertUserStatements(statements);
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.TRANSFER;
    }
}
