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
import com.diligrp.xtrade.upay.trade.service.IPaymentProtocolService;
import com.diligrp.xtrade.upay.trade.service.IPaymentService;
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
    private IUserStatementDao userStatementDao;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IPaymentProtocolService paymentProtocolService;

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
        if (!ChannelType.forFee(payment.getChannelId())) {
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

        // 处理账户余额缴费
        UserAccount account = null;
        LocalDateTime now = LocalDateTime.now().withNano(0);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        if (!ChannelType.ACCOUNT.equalTo(payment.getChannelId())) { // 非账户缴费不校验密码（办卡/换卡工本费）
            account = accountChannelService.checkTradePermission(payment.getAccountId());
        } else if (payment.getProtocolId() == null) { // 交易密码校验
            account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), maxPwdErrors);
        } else { // 免密支付
            account = paymentProtocolService.checkProtocolPermission(payment.getAccountId(),
                payment.getProtocolId(), payment.getAmount());
        }

        TransactionStatus status = null;
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        if (payment.getChannelId() == ChannelType.ACCOUNT.getCode()) {
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            fees.forEach(fee -> transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            status = accountChannelService.submit(transaction);
        }

        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.SUCCESS.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }

        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId()).name(trade.getName())
            .cardNo(null).amount(trade.getAmount()).fee(0L).protocolId(payment.getProtocolId())
            .state(PaymentState.SUCCESS.getCode()).description(TradeType.PAY_FEE.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        List<PaymentFee> paymentFeeDos = fees.stream().map(fee ->
            PaymentFee.of(paymentId, fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(paymentFeeDos);

        // 只有通过账户余额进行缴费才生成缴费账户业务账单
        if (payment.getChannelId() == ChannelType.ACCOUNT.getCode()) {
            String typeName = StatementType.PAY_FEE.getName() + (ObjectUtils.isEmpty(trade.getDescription())
                ? "" : "-" + trade.getDescription());
            UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
                .channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId(), account.getParentId())
                .type(StatementType.PAY_FEE.getCode()).typeName(typeName).amount(- totalFee).fee(0L)
                .balance(status.getBalance() + status.getAmount()).frozenAmount(status.getFrozenBalance() + status.getFrozenAmount())
                .serialNo(trade.getSerialNo()).state(4).createdTime(now).build();
            userStatementDao.insertUserStatement(statement);
        }

        // 处理商户收款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
        IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
        fees.forEach(fee -> feeTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
        accountChannelService.submitExclusively(feeTransaction);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * 撤销交易-资金做逆向操作，商户退缴费金额
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        if (trade.getState() != TradeState.SUCCESS.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效的交易状态，不能进行撤销操作");
        }

        // "缴费"不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        List<PaymentFee> fees = paymentFeeDao.findPaymentFees(payment.getPaymentId());
        long totalFees = fees.stream().mapToLong(PaymentFee::getAmount).sum();
        if (totalFees != payment.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "撤销金额与支付金额不一致");
        }

        // 撤销缴费，需验证缴费账户状态无须验证密码
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UserAccount account = accountChannelService.checkTradePermission(trade.getAccountId());
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());

        // 处理客户收款
        TransactionStatus status = null;
        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId())) {
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
            fees.forEach(fee -> transaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
            status = accountChannelService.submit(transaction);
        }

        RefundPayment refund = RefundPayment.builder().paymentId(paymentId).type(TradeType.CANCEL_TRADE.getCode())
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
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), 0L, null,
            TradeState.CANCELED.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }

        // 生成退款账户业务账单
        String typeName = ObjectUtils.isNull(trade.getDescription()) ? StatementType.PAY_FEE.getName() + "-"
            + StatementType.REFUND.getName() : trade.getDescription() + "-" + StatementType.REFUND.getName();
        if (ChannelType.ACCOUNT.equalTo(payment.getChannelId())) {
            UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId)
                .channelId(payment.getChannelId()).accountId(payment.getAccountId(), account.getParentId())
                .type(StatementType.REFUND.getCode()).typeName(typeName).amount(totalFees).fee(0L)
                .balance(status.getBalance() + status.getAmount()).frozenAmount(status.getFrozenBalance()
                + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4).createdTime(now).build();
            userStatementDao.insertUserStatement(statement);
        }

        // 处理商户退款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
        IFundTransaction feeTransaction = merChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
        fees.forEach(fee -> feeTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
        accountChannelService.submitExclusively(feeTransaction);
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.PAY_FEE;
    }
}
