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
import java.util.List;
import java.util.Optional;

/**
 * 预存款业务：与充值业务类似，支持现金、POS和网银进行预存款充值，且暂不支持费用收取
 */
@Service("preDepositPaymentService")
public class PredepositPaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IUserStatementDao userStatementDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     * 预存款充值时提交充值时资金账号与创建预存款交易的资金账号需一致，且暂不支持收取费用
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forDeposit(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行预存款业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "预存款资金账号不一致");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        feesOpt.ifPresent(fees -> { throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "预存款暂不支持收取费用"); });

        // 处理个人充值
        LocalDateTime now = LocalDateTime.now();
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId());
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
        transaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        TransactionStatus status = accountChannelService.submit(transaction);

        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.SUCCESS.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId())
            .name(trade.getName()).cardNo(null).amount(payment.getAmount()).fee(0L).state(PaymentState.SUCCESS.getCode())
            .description(tradeName(payment.getChannelId())).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        // 生成存款账户业务账单
        UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
            .channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId(), account.getParentId())
            .type(StatementType.DEPOSIT.getCode()).typeName(StatementType.DEPOSIT.getName())
            .amount(trade.getAmount()).fee(0L).balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).build();
        userStatementDao.insertUserStatement(statement);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    private String tradeName(int channelType) {
        if (channelType == ChannelType.CASH.getCode()) {
            return "现金预存款";
        } else if (channelType == ChannelType.POS.getCode()) {
            return "POS预存款";
        } else if (channelType == ChannelType.E_BANK.getCode()) {
            return "网银预存款";
        }
        return supportType().getName();
    }

    @Override
    public TradeType supportType() {
        return TradeType.PRE_DEPOSIT;
    }
}
