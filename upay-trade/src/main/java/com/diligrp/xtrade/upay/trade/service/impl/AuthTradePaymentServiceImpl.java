package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.sequence.ISerialKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.FrozenStateDto;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.channel.type.FrozenState;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IMerchantDao;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.domain.Confirm;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.domain.PaymentStateDto;
import com.diligrp.xtrade.upay.trade.domain.Refund;
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
import com.diligrp.xtrade.upay.trade.util.PaymentDatedIdStrategy;
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
 * 预授权交易：先对买方账户进行资金冻结，然后解冻并完成交易或取消交易并解冻资金。实际交易金额可以大于冻结金额。
 * 业务场景：交易过磅时冻结资金（prepare->commit)，回皮后解冻并实际交易（confirm)或取消交易(cancel)
 */
@Service("authTradePaymentService")
public class AuthTradePaymentServiceImpl extends TradePaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IMerchantDao merchantDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    /**
     * {@inheritDoc}
     *
     * 提交预授权交易只冻结资金，不进行实际交易；
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forTrade(ChannelType.ACCOUNT.getCode())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行预授权交易业务");
        }
        if (trade.getAccountId().equals(payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "同一账号不能进行交易");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        feesOpt.ifPresent(fees -> { throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "预授权冻结不支持收取费用"); });

        // 冻结资金
        LocalDateTime now = LocalDateTime.now();
        FundAccount fromAccount = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), 5);
        ISerialKeyGenerator keyGenerator = keyGeneratorManager.getSerialKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextSerialNo(new PaymentDatedIdStrategy(trade.getType()));
        AccountChannel channel = AccountChannel.of(paymentId, payment.getAccountId(), payment.getBusinessId());
        IFundTransaction transaction = channel.openTransaction(FrozenState.FROZEN.getCode(), now);
        transaction.freeze(trade.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);

        // 创建冻结资金订单
        long frozenId = keyGeneratorManager.getKeyGenerator(SequenceKey.FROZEN_ID).nextId();
        FrozenOrder frozenOrder = FrozenOrder.builder().frozenId(frozenId).paymentId(paymentId)
            .accountId(payment.getAccountId()).name(fromAccount.getName()).type(FrozenType.TRADE_FROZEN.getCode())
            .amount(trade.getAmount()).state(FrozenState.FROZEN.getCode()).description(null)
            .version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);

        // 冻结交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.FROZEN.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        // 生成"待处理"支付的支付记录
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(payment.getAccountId()).businessId(payment.getBusinessId())
            .name(fromAccount.getName()).cardNo(null).amount(payment.getAmount()).fee(0L).state(PaymentState.PENDING.getCode())
            .description(TradeType.AUTH_TRADE.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * "预授权交易"业务确认预授权交易(交易冻结后确认实际交易金额)，当前业务场景允许实际交易金额大于冻结金额
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult confirm(TradeOrder trade, Confirm confirm) {
        Optional<List<Fee>> feesOpt = confirm.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElseGet(Collections::emptyList);
        fees.forEach(Fee::checkUseFor);

        // "预授权交易"不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (!payment.getAccountId().equals(confirm.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "预授权支付账号不一致");
        }

        // 查询冻结订单
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findFrozenOrderByPaymentId(payment.getPaymentId());
        FrozenOrder frozenOrder = orderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (frozenOrder.getState() != FrozenState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无预授权资金记录");
        }

        // 获取商户收益账号信息
        LocalDateTime now = LocalDateTime.now();
        accountChannelService.checkTradePermission(payment.getAccountId(), confirm.getPassword(), 5);
        MerchantPermit merchant = merchantDao.findMerchantById(trade.getMchId()).map(mer -> MerchantPermit.of(
            mer.getMchId(), mer.getProfitAccount(), mer.getVouchAccount(), mer.getPledgeAccount(), mer.getPrivateKey(),
            mer.getPublicKey())).orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));

        // 处理买家付款和买家佣金
        AccountChannel fromChannel = AccountChannel.of(payment.getPaymentId(), payment.getAccountId(), payment.getBusinessId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(trade.getType(), now);
        fromTransaction.unfreeze(frozenOrder.getAmount());
        fromTransaction.outgo(confirm.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        fees.stream().filter(Fee::forBuyer).forEach(fee -> {
            fromTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName());
        });
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 处理卖家收款和卖家佣金
        AccountChannel toChannel = AccountChannel.of(payment.getPaymentId(), trade.getAccountId(), trade.getBusinessId());
        IFundTransaction toTransaction = toChannel.openTransaction(trade.getType(), now);
        toTransaction.income(confirm.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        fees.stream().filter(Fee::forSeller).forEach(fee -> {
            toTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName());
        });
        accountChannelService.submit(toTransaction);

        // 处理商户收益
        if (!fees.isEmpty()) {
            AccountChannel merChannel = AccountChannel.of(payment.getPaymentId(), merchant.getProfitAccount(), null);
            IFundTransaction merTransaction = merChannel.openTransaction(trade.getType(), now);
            fees.forEach(fee ->
                merTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName())
            );
            accountChannelService.submit(merTransaction);
        }

        // 修改冻结订单"已解冻"状态
        FrozenStateDto frozenState = FrozenStateDto.of(frozenOrder.getFrozenId(), FrozenState.UNFROZEN.getCode(),
            frozenOrder.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // 卖家佣金存储在TradeOrder订单模型中
        long toFee = fees.stream().filter(Fee::forSeller).mapToLong(Fee::getAmount).sum();
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), confirm.getAmount(), toFee,
            TradeState.SUCCESS.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        // 买家佣金存储在TradePayment支付模型中
        long fromFee = fees.stream().filter(Fee::forBuyer).mapToLong(Fee::getAmount).sum();
        PaymentStateDto paymentState = PaymentStateDto.of(payment.getPaymentId(), confirm.getAmount(), fromFee,
            PaymentState.SUCCESS.getCode(), payment.getVersion(), now);
        result = tradePaymentDao.compareAndSetState(paymentState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        if (!fees.isEmpty()) {
            List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
                PaymentFee.of(payment.getPaymentId(), fee.getUseFor(), fee.getAmount(), fee.getType(), fee.getTypeName(), now)
            ).collect(Collectors.toList());
            paymentFeeDao.insertPaymentFees(paymentFeeDos);
        }

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, payment.getPaymentId(), status);
    }

    /**
     * {@inheritDoc}
     *
     * 撤销交易-确认预授权后撤销退交易资金和佣金，确认预授权前只解冻资金
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        if (trade.getState() == TradeState.SUCCESS.getCode()) {
            return super.cancel(trade, cancel);
        }
        if (!trade.getAccountId().equals(cancel.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "撤销账号不一致");
        }

        // "预授权交易"不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));

        // 验证退款方密码, 查询冻结订单
        LocalDateTime when = LocalDateTime.now();
        accountChannelService.checkTradePermission(trade.getAccountId(), cancel.getPassword(), 5);
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findFrozenOrderByPaymentId(payment.getPaymentId());
        FrozenOrder order = orderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (order.getState() != FrozenState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效交易状态，不能执行该操作");
        }

        // 解冻冻结资金
        AccountChannel channel = AccountChannel.of(payment.getPaymentId(), payment.getAccountId(), payment.getBusinessId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), when);
        transaction.unfreeze(trade.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);
        // 修改冻结订单状态
        FrozenStateDto frozenState = FrozenStateDto.of(order.getFrozenId(), FrozenState.UNFROZEN.getCode(),
            order.getVersion(), when);
        if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // 撤销支付记录
        PaymentStateDto paymentState = PaymentStateDto.of(payment.getPaymentId(), PaymentState.CANCELED.getCode(),
            payment.getVersion(), when);
        if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // 撤销交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.CANCELED.getCode(), trade.getVersion(), when);
        if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, payment.getPaymentId(), status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.AUTH_TRADE;
    }
}
