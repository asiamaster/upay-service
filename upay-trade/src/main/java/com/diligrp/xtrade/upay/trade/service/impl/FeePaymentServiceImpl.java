package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.sequence.ISerialKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IMerchantDao;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
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
import com.diligrp.xtrade.upay.trade.type.PaymentState;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import com.diligrp.xtrade.upay.trade.type.TradeType;
import com.diligrp.xtrade.upay.trade.util.PaymentDatedIdStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 缴费业务：允许使用账户/余额、现金进行缴费业务
 */
@Service("feePaymentService")
public class FeePaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IRefundPaymentDao refundPaymentDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IMerchantDao merchantDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    /**
     * {@inheritDoc}
     *
     * 提交缴费时的费用入商户收益账户
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forFee(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行缴费业务");
        }
        if (!trade.getAccountId().equals(payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费账号不一致");
        }

        MerchantPermit merchant = payment.getObject(MerchantPermit.class.getName(), MerchantPermit.class);
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无收费信息"));
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != payment.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际缴费金额与申请缴费金额不一致");
        }

        // 处理账户余额缴费
        TransactionStatus status = null;
        LocalDateTime now = LocalDateTime.now();
        accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), 5);
        ISerialKeyGenerator keyGenerator = keyGeneratorManager.getSerialKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextSerialNo(new PaymentDatedIdStrategy(trade.getType()));
        if (payment.getChannelId() == ChannelType.ACCOUNT.getCode()) {
            AccountChannel channel = AccountChannel.of(paymentId, trade.getAccountId(), trade.getBusinessId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            fees.forEach(fee ->
                transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName())
            );
            status = accountChannelService.submit(transaction);
        }

        // 处理商户收款
        AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), null);
        IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
        fees.forEach(fee ->
            feeTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName())
        );
        accountChannelService.submit(feeTransaction);

        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.SUCCESS.getCode(),
            trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }

        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId()).businessId(trade.getBusinessId())
            .name(trade.getName()).cardNo(null).amount(trade.getAmount()).fee(0L).state(PaymentState.SUCCESS.getCode())
            .description(TradeType.FEE.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(paymentFeeDos);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * 撤销交易-资金做逆向操作，商户退缴费金额
     */
    @Override
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        if (trade.getState() != TradeState.SUCCESS.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效的交易状态，不能进行撤销操作");
        }
        if (!trade.getAccountId().equals(cancel.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "退款账号不一致");
        }

        // "缴费"不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        List<PaymentFee> fees = paymentFeeDao.findPaymentFees(payment.getPaymentId());
        long totalFees = fees.stream().mapToLong(PaymentFee::getAmount).sum();
        if (totalFees != payment.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "撤销金额与支付金额不一致");
        }

        // 验证退款方密码, 获取交易订单中的商户收益账号信息，并处理商户退款
        LocalDateTime now = LocalDateTime.now();
        accountChannelService.checkTradePermission(trade.getAccountId(), cancel.getPassword(), 5);
        MerchantPermit merchant = merchantDao.findMerchantById(trade.getMchId()).map(mer -> MerchantPermit.of(
            mer.getMchId(), mer.getProfitAccount(), mer.getVouchAccount(), mer.getPledgeAccount(), mer.getPrivateKey(),
            mer.getPublicKey())).orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));
        ISerialKeyGenerator keyGenerator = keyGeneratorManager.getSerialKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = keyGenerator.nextSerialNo(new PaymentDatedIdStrategy(trade.getType()));
        AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), null);
        IFundTransaction feeTransaction = merChannel.openTransaction(TradeType.CANCEL.getCode(), now);
        fees.forEach(fee ->
            feeTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName())
        );
        accountChannelService.submit(feeTransaction);

        // 处理客户收款
        TransactionStatus status = null;
        if (payment.getChannelId() == ChannelType.ACCOUNT.getCode()) {
            AccountChannel channel = AccountChannel.of(paymentId, trade.getAccountId(), trade.getBusinessId());
            IFundTransaction transaction = channel.openTransaction(TradeType.CANCEL.getCode(), now);
            fees.forEach(fee ->
                transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName())
            );
            status = accountChannelService.submit(transaction);
        }

        RefundPayment refund = RefundPayment.builder().paymentId(paymentId).type(TradeType.CANCEL.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).amount(totalFees).fee(0L)
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
        return TradeType.FEE;
    }
}
