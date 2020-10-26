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
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.domain.TradeStateDto;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.PaymentFee;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 账户提现业务：支持现金和网银提现
 */
@Service("withdrawPaymentService")
public class WithdrawPaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IUserStatementDao userStatementDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     * 只支持现金和网银渠道，且提现费用入商户收益账户
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forWithdraw(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行提现业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "提现资金账号不一致");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElseGet(Collections::emptyList);

        // 处理个人提现
        LocalDateTime now = LocalDateTime.now();
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), -1);
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
        transaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        fees.forEach(fee -> {
            transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName());
        });
        TransactionStatus status = accountChannelService.submit(transaction);

        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.SUCCESS.getCode(),
            trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId()).name(trade.getName())
            .cardNo(null).amount(payment.getAmount()).fee(totalFee).state(PaymentState.SUCCESS.getCode())
            .description(TradeType.WITHDRAW.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        if (!fees.isEmpty()) {
            List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
                PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), now)
            ).collect(Collectors.toList());
            paymentFeeDao.insertPaymentFees(paymentFeeDos);
        }

        // 生成提现账户的业务账单
        UserStatement statement = UserStatement.builder().appId(trade.getAppId()).tradeId(trade.getTradeId())
            .paymentId(paymentDo.getPaymentId()).channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId())
            .type(StatementType.WITHDRAW.getCode()).typeName(StatementType.WITHDRAW.getName())
            .amount(-trade.getAmount() - totalFee).fee(totalFee).balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).build();
        userStatementDao.insertUserStatement(statement);

        // 处理商户收益 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        if (!fees.isEmpty()) {
            MerchantPermit merchant = payment.getObject(MerchantPermit.class.getName(), MerchantPermit.class);
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction merTransaction = merChannel.openTransaction(trade.getType(), now);
            fees.forEach(fee ->
                merTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName())
            );
            accountChannelService.submit(merTransaction);
        }

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.WITHDRAW;
    }
}
