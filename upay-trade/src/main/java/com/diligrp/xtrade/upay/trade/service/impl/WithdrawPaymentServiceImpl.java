package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.shared.util.AssertUtils;
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
import com.diligrp.xtrade.upay.trade.domain.Correct;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.domain.PaymentStateDto;
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
import java.util.concurrent.atomic.AtomicLong;
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
    private IRefundPaymentDao refundPaymentDao;

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
        LocalDateTime now = LocalDateTime.now().withNano(0);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), maxPwdErrors);
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
        transaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
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
                PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
            ).collect(Collectors.toList());
            paymentFeeDao.insertPaymentFees(paymentFeeDos);
        }

        // 生成提现账户的业务账单
        UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
            .channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId(), account.getParentId())
            .type(StatementType.WITHDRAW.getCode()).typeName(StatementType.WITHDRAW.getName())
            .amount(-trade.getAmount() - totalFee).fee(totalFee).balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).build();
        userStatementDao.insertUserStatement(statement);

        // 处理商户收益 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        if (!fees.isEmpty()) {
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction merTransaction = merChannel.openTransaction(trade.getType(), now);
            fees.forEach(fee -> merTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
            accountChannelService.submitExclusively(merTransaction);
        }

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult correct(TradeOrder trade, Correct correct) {
        // 冲正参数检查
        AssertUtils.isTrue(ObjectUtils.equals(trade.getAccountId(), correct.getAccountId()), "冲正资金账号不一致");
        AssertUtils.isTrue(correct.getAmount() > 0, "提现冲正金额非法");
        AssertUtils.isTrue(trade.getAmount() - correct.getAmount() >= 0, "冲正金额不能大于原操作金额");
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        Optional<Fee> feeOpt = correct.getObject(Fee.class.getName());
        feeOpt.ifPresent(fee -> {
            AssertUtils.isTrue(fee.getAmount() < 0, "提现冲正费用非法");
            AssertUtils.isTrue(fee.getAmount() + payment.getFee() >= 0, "冲正费用不能大于原提现费用");
        });

        // 处理原账户的冲正, 账户入账金额 = 冲正金额-冲正费用(负数)
        LocalDateTime now = LocalDateTime.now().withNano(0);
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        UserAccount account = fundAccountService.findUserAccountById(correct.getAccountId());
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(TradeType.CORRECT_TRADE.getCode(), now);
        transaction.income(correct.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        feeOpt.ifPresent(fee -> transaction.income(Math.abs(fee.getAmount()), fee.getType(), fee.getTypeName(), fee.getDescription()));
        TransactionStatus status = accountChannelService.submit(transaction);

        // 计算正确的提现金额和费用, 真实提现金额=原提现金额-冲正金额, 真实充值费用=原充值费用+冲正费用(负数)
        Long newAmount = trade.getAmount() - correct.getAmount();
        AtomicLong newFee = new AtomicLong(payment.getFee());
        feeOpt.ifPresent(fee -> newFee.addAndGet(fee.getAmount()));

        RefundPayment refund = RefundPayment.builder().paymentId(paymentId).type(TradeType.CORRECT_TRADE.getCode())
            .tradeId(trade.getTradeId()).tradeType(trade.getType()).amount(correct.getAmount()).fee(0L)
            .state(TradeState.SUCCESS.getCode()).description(null).version(0).createdTime(now).build();
        refundPaymentDao.insertRefundPayment(refund);

        // 更正支付记录并生成冲正费用项
        PaymentStateDto paymentState = PaymentStateDto.of(payment.getPaymentId(), newAmount, newFee.get(), null,
            payment.getVersion(), now);
        if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        feeOpt.ifPresent(fee -> {
            paymentFeeDao.insertPaymentFee(PaymentFee.of(payment.getPaymentId(), fee.getAmount(), fee.getType(),
                fee.getTypeName(), fee.getDescription(), now));
        });

        // 更正交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), newAmount, null, null, null,
            trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // 生成交易冲正时账户业务账单
        AtomicLong totalAmount = new AtomicLong(correct.getAmount());
        feeOpt.ifPresent(fee -> totalAmount.addAndGet(Math.abs(fee.getAmount())));
        String typeName = StatementType.CORRECT.getName() + "-" + StatementType.WITHDRAW.getName();
        UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId)
            .channelId(payment.getChannelId()).accountId(payment.getAccountId(), account.getParentId())
            .type(StatementType.CORRECT.getCode()).typeName(typeName).amount(totalAmount.get()).fee(0L)
            .balance(status.getBalance() + status.getAmount()).frozenAmount(status.getFrozenBalance()
                + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4).createdTime(now).build();
        feeOpt.ifPresent(fee -> statement.setFee(fee.getAmount()));
        userStatementDao.insertUserStatement(statement);

        // 处理商户收益 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        feeOpt.ifPresent(fee -> {
            MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction feeTransaction = merChannel.openTransaction(TradeType.CORRECT_TRADE.getCode(), now);
            feeTransaction.outgo(Math.abs(fee.getAmount()), fee.getType(), fee.getTypeName(), null);
            accountChannelService.submitExclusively(feeTransaction);
        });

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.WITHDRAW;
    }
}
