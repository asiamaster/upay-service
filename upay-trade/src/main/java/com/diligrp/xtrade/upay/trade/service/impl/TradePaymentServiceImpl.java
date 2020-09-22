package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IMerchantDao;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.trade.dao.IPaymentFeeDao;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 即时交易业务：交易资金即时到帐且支持收取买卖家交易佣金
 */
@Service("tradePaymentService")
public class TradePaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IRefundPaymentDao refundPaymentDao;

    @Resource
    private IMerchantDao merchantDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     * 支持买卖家同时收取交易佣金，交易佣金直接入商户收益账户
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forTrade(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行即时交易业务");
        }
        if (trade.getAccountId().equals(payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "同一账号不能进行交易");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElse(Collections.emptyList());
        fees.forEach(Fee::checkUseFor);

        // 处理买家付款和买家佣金
        LocalDateTime now = LocalDateTime.now();
        FundAccount fromAccount = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), -1);
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑
        if (!ObjectUtils.equals(fromAccount.getMchId(), trade.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能进行跨商户交易");
        }
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel fromChannel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(trade.getType(), now);
        fromTransaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        fees.stream().filter(Fee::forBuyer).forEach(fee -> {
            fromTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName());
        });
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 处理卖家收款和卖家佣金
        FundAccount toAccount = fundAccountService.findFundAccountById(trade.getAccountId());
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        AccountChannel toChannel = AccountChannel.of(paymentId, toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(trade.getType(), now);
        toTransaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        fees.stream().filter(Fee::forSeller).forEach(fee -> {
            toTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName());
        });
        status.setRelation(accountChannelService.submit(toTransaction));

        // 处理商户收益
        if (!fees.isEmpty()) {
            MerchantPermit merchant = payment.getObject(MerchantPermit.class.getName(), MerchantPermit.class);
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction merTransaction = merChannel.openTransaction(trade.getType(), now);
            fees.forEach(fee ->
                merTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName())
            );
            accountChannelService.submit(merTransaction);
        }

        // 卖家佣金存储在TradeOrder订单模型中
        long toFee = fees.stream().filter(Fee::forSeller).mapToLong(Fee::getAmount).sum();
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, toFee,
            TradeState.SUCCESS.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }

        // 买家佣金存储在TradePayment支付模型中
        long fromFee = fees.stream().filter(Fee::forBuyer).mapToLong(Fee::getAmount).sum();
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(payment.getAccountId())
            .name(fromAccount.getName()).cardNo(null).amount(payment.getAmount()).fee(fromFee).state(PaymentState.SUCCESS.getCode())
            .description(TradeType.DIRECT_TRADE.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);
        if (!fees.isEmpty()) {
            List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
                    PaymentFee.of(paymentId, fee.getUseFor(), fee.getAmount(), fee.getType(), fee.getTypeName(), now)
            ).collect(Collectors.toList());
            paymentFeeDao.insertPaymentFees(paymentFeeDos);
        }

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * 撤销交易-退交易资金和佣金，交易撤销需要修改交易订单状态
     */
    @Override
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        if (trade.getState() != TradeState.SUCCESS.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效的交易状态，不能进行撤销操作");
        }

        // "即时交易"业务不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));

        // 撤销交易，需验证退款方账户状态无须验证密码
        LocalDateTime now = LocalDateTime.now();
        FundAccount fromAccount = accountChannelService.checkTradePermission(trade.getAccountId());
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑
        MerchantPermit merchant = merchantDao.findMerchantById(trade.getMchId()).map(mer -> MerchantPermit.of(
            mer.getMchId(), mer.getCode(), mer.getProfitAccount(), mer.getVouchAccount(), mer.getPledgeAccount(),
            mer.getPrivateKey(), mer.getPublicKey()))
            .orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());

        // 处理卖家退款和退佣金，由于底层先产生收入明细后产生支出明细(FundActivity.compare)
        // 这样保证卖家先退款后收入佣金不会造成收支明细中期初余额出现负数（资金仍然是安全的）
        List<PaymentFee> fees = paymentFeeDao.findPaymentFees(payment.getPaymentId());
        AccountChannel fromChannel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
        fromTransaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        fees.stream().filter(PaymentFee::forSeller).forEach(fee -> {
            fromTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName());
        });
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 处理买家收款和退佣金
        FundAccount toAccount = fundAccountService.findFundAccountById(payment.getAccountId());
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        AccountChannel toChannel = AccountChannel.of(paymentId, toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
        fees.stream().filter(PaymentFee::forBuyer).forEach(fee -> {
            toTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName());
        });
        toTransaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        status.setRelation(accountChannelService.submit(toTransaction));

        // 处理商户退佣金
        if (!fees.isEmpty()) {
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction merTransaction = merChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
            fees.forEach(fee ->
                merTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName())
            );
            accountChannelService.submit(merTransaction);
        }

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
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.DIRECT_TRADE;
    }
}
