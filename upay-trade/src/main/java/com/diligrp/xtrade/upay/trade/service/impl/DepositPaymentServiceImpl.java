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
 * 充值业务：允许现金、POS和网银进行账户充值
 */
@Service("depositPaymentService")
public class DepositPaymentServiceImpl implements IPaymentService {

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
     * 提交充值时充值账号需与创建充值申请时资金账号一致
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forDeposit(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行充值业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "充值资金账号不一致");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElseGet(Collections::emptyList);

        // 处理个人充值
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), -1);
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
        transaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
        fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName()));
        TransactionStatus status = accountChannelService.submit(transaction);

        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.SUCCESS.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(account.getAccountId())
            .name(trade.getName()).cardNo(null).amount(payment.getAmount()).fee(totalFee).state(PaymentState.SUCCESS.getCode())
            .description(tradeName(payment.getChannelId())).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);
        if (!fees.isEmpty()) {
            List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
                PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), now)
            ).collect(Collectors.toList());
            paymentFeeDao.insertPaymentFees(paymentFeeDos);
        }

        // 生成充值账户的业务账单
        UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
            .channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId(), account.getParentId())
            .type(StatementType.DEPOSIT.getCode()).typeName(StatementType.DEPOSIT.getName())
            .amount(trade.getAmount() - totalFee).fee(totalFee).balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).build();
        userStatementDao.insertUserStatement(statement);

        // 处理商户收益 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        if (!fees.isEmpty()) {
            MerchantPermit merchant = payment.getObject(MerchantPermit.class.getName(), MerchantPermit.class);
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
            fees.forEach(fee -> feeTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName()));
            accountChannelService.submitExclusively(feeTransaction);
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
        AssertUtils.isTrue(correct.getAmount() < 0, "充值冲正金额非法");
        AssertUtils.isTrue(correct.getAmount() + trade.getAmount() >= 0, "冲正金额不能大于原操作金额");
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        Optional<Fee> feeOpt = correct.getObject(Fee.class.getName());
        feeOpt.ifPresent(fee -> {
            AssertUtils.isTrue(fee.getAmount() < 0, "充值冲正费用非法");
            AssertUtils.isTrue(fee.getAmount() + payment.getAmount() >= 0, "冲正费用不能大于原充值费用");
        });

        // 处理原账户的冲正, 账户出账金额 = ABS(冲正金额(负数)-冲正费用(负数))
        LocalDateTime now = LocalDateTime.now().withNano(0);
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        UserAccount account = fundAccountService.findUserAccountById(correct.getAccountId());
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(TradeType.CORRECT_TRADE.getCode(), now);
        transaction.outgo(Math.abs(correct.getAmount()), FundType.FUND.getCode(), FundType.FUND.getName());
        feeOpt.ifPresent(fee -> transaction.income(Math.abs(fee.getAmount()), fee.getType(), fee.getTypeName()));
        TransactionStatus status = accountChannelService.submit(transaction);

        // 计算正确的充值金额和费用, 真实充值金额=原充值金额+冲正金额(负数), 真实充值费用=原充值费用+冲正费用(负数)
        Long newAmount = trade.getAmount() + correct.getAmount();
        AtomicLong newFee = new AtomicLong(payment.getFee());
        feeOpt.ifPresent(fee -> newFee.addAndGet(fee.getAmount()));
        // 生成冲正记录
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
                fee.getTypeName(), now));
        });

        // 更正交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), newAmount, null, null, null,
            trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // 计算实际操作金额, 生成交易冲正时账户业务账单
        AtomicLong totalAmount = new AtomicLong(correct.getAmount());
        feeOpt.ifPresent(fee -> totalAmount.addAndGet(Math.abs(fee.getAmount())));
        String typeName = StatementType.DEPOSIT.getName() + "-" + StatementType.CORRECT.getName();
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
            feeTransaction.outgo(Math.abs(fee.getAmount()), fee.getType(), fee.getTypeName());
            accountChannelService.submitExclusively(feeTransaction);
        });

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    private String tradeName(int channelType) {
        if (channelType == ChannelType.CASH.getCode()) {
            return "现金充值";
        } else if (channelType == ChannelType.POS.getCode()) {
            return "POS充值";
        } else if (channelType == ChannelType.E_BANK.getCode()) {
            return "网银充值";
        }
        return supportType().getName();
    }

    @Override
    public TradeType supportType() {
        return TradeType.DEPOSIT;
    }
}
