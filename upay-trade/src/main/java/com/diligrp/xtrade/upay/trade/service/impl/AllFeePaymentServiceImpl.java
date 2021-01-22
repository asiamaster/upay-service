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
import com.diligrp.xtrade.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.xtrade.upay.trade.dao.IRefundPaymentDao;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
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
 * 综合缴费业务：费用允许使用订金抵扣
 */
@Service("allFeePaymentService")
public class AllFeePaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IRefundPaymentDao refundPaymentDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IUserStatementDao userStatementDao;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     * 提交缴费时的费用入商户收益账户
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forAllFee(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行缴费业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费资金账号不一致");
        }

        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无收费信息"));
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != payment.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际缴费金额与申请缴费金额不一致");
        }

        Optional<List<Fee>> deductFeesOpt = payment.getObjects(Fee.class.getName() + ".deduct");
        List<Fee> deductFees = deductFeesOpt.orElse(Collections.emptyList());
        long totalDeductAmount = deductFees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee < totalDeductAmount) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费金额应不小于抵扣金额");
        }

        // 处理账户余额缴费
        UserAccount account = null;
        TransactionStatus status = null;
        LocalDateTime now = LocalDateTime.now().withNano(0);
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId())) {
            account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), maxPwdErrors);
            accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            deductFees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            status = accountChannelService.submit(transaction);
        }

        // 综合收费的抵扣金额存放在trade_order.fee和trade_payment.fee中, 当发生退还抵扣金额时(交易退款)修改trade_order.fee为剩余抵扣金额
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, null, totalDeductAmount,
            TradeState.SUCCESS.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId()).name(trade.getName())
            .cardNo(null).amount(trade.getAmount()).fee(totalDeductAmount).state(PaymentState.SUCCESS.getCode())
            .description(TradeType.ALL_FEE.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(paymentFeeDos);
        List<PaymentFee> deductList = deductFees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        if (!deductList.isEmpty()) {
            paymentFeeDao.insertDeductFees(deductList);
        }

        // 只有通过账户余额进行缴费才生成缴费账户业务账单
        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId()) && totalFee - totalDeductAmount > 0) {
            UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
                .channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId(), account.getParentId())
                .type(StatementType.PAY_FEE.getCode()).typeName(StatementType.PAY_FEE.getName())
                .amount(totalDeductAmount - totalFee).fee(-totalDeductAmount).balance(status.getBalance() + status.getAmount())
                .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo())
                .state(4).createdTime(now).build();
            userStatementDao.insertUserStatement(statement);
        }

        // 处理商户收款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
        IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
        fees.forEach(fee -> feeTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
        deductFees.forEach(fee -> feeTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
        accountChannelService.submitExclusively(feeTransaction);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * 交易退款-允许部分退款
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult refund(TradeOrder trade, Refund refund) {
        Optional<List<Fee>> feesOpt = refund.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无退费信息"));
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee != refund.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "实际退费金额与申请退费金额不一致");
        }

        Optional<List<Fee>> deductFeesOpt = refund.getObjects(Fee.class.getName() + ".deduct");
        List<Fee> deductFees = deductFeesOpt.orElse(Collections.emptyList());
        long totalDeductAmount = deductFees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee < totalDeductAmount) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "申请退费金额应不小于申请退回的抵扣金额");
        }

        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (trade.getAmount() < totalFee) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "申请退费金额超过原支付金额");
        }
        if (trade.getFee() < totalDeductAmount) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "申请退回的抵扣金额超过原抵扣金额");
        }

        // 处理账户余额缴费
        UserAccount account = null;
        TransactionStatus status = null;
        LocalDateTime now = LocalDateTime.now().withNano(0);
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId())) {
            account = fundAccountService.findUserAccountById(payment.getAccountId());
            accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            fees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            deductFees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            status = accountChannelService.submit(transaction);
        }

        RefundPayment request = RefundPayment.builder().paymentId(paymentId).type(TradeType.REFUND_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).amount(totalFee).fee(totalDeductAmount)
            .state(TradeState.SUCCESS.getCode()).description(null).version(0).createdTime(now).build();
        refundPaymentDao.insertRefundPayment(request);
        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(paymentFeeDos);
        List<PaymentFee> deductList = deductFees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        if (!deductList.isEmpty()) {
            paymentFeeDao.insertDeductFees(deductList);
        }

        // 更新原交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), trade.getAmount() - totalFee, null,
            trade.getFee() - totalDeductAmount, TradeState.REFUND.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }

        // 只有通过账户余额进行缴费退款时才生成账户业务账单
        String typeName = ObjectUtils.isEmpty(trade.getDescription()) ? StatementType.PAY_FEE.getName() + "-"
            + StatementType.REFUND.getName() : trade.getDescription() + "-" + StatementType.REFUND.getName();
        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId()) && totalFee - totalDeductAmount > 0) {
            UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId)
                .channelId(ChannelType.ACCOUNT.getCode()).accountId(account.getAccountId(), account.getParentId())
                .type(StatementType.REFUND.getCode()).typeName(typeName).amount(totalFee - totalDeductAmount)
                .fee(totalDeductAmount).balance(status.getBalance() + status.getAmount())
                .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo())
                .state(4).createdTime(now).build();
            userStatementDao.insertUserStatement(statement);
        }

        // 处理商户收款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
        IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
        fees.forEach(fee -> feeTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
        deductFees.forEach(fee -> feeTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
        accountChannelService.submitExclusively(feeTransaction);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.ALL_FEE;
    }
}
