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
import com.diligrp.xtrade.upay.core.type.Permission;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.DataPartition;
import com.diligrp.xtrade.upay.sentinel.domain.Passport;
import com.diligrp.xtrade.upay.sentinel.domain.RiskControlEngine;
import com.diligrp.xtrade.upay.sentinel.service.IRiskControlService;
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
import java.util.ArrayList;
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
    private IUserStatementDao userStatementDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IRiskControlService riskControlService;

    @Resource
    private IAccessPermitService accessPermitService;

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
        LocalDateTime now = LocalDateTime.now().withNano(0);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        UserAccount fromAccount = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), maxPwdErrors);
        UserAccount toAccount = fundAccountService.findUserAccountById(trade.getAccountId());
        if (!ObjectUtils.equals(fromAccount.getMchId(), toAccount.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能进行跨商户交易");
        }
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        // 风控检查
        toAccount.checkPermission(Permission.FOR_TRADE); // 检查卖家交易权限
        RiskControlEngine riskControlEngine = riskControlService.loadRiskControlEngine(fromAccount);
        Passport passport = Passport.ofTrade(fromAccount.getAccountId(), fromAccount.getPermission(), payment.getAmount());
        riskControlEngine.checkPassport(passport);

        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel fromChannel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(trade.getType(), now);
        fromTransaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        fees.stream().filter(Fee::forBuyer).forEach(fee ->
            fromTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 处理卖家收款和卖家佣金
        AccountChannel toChannel = AccountChannel.of(paymentId, toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(trade.getType(), now);
        toTransaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        fees.stream().filter(Fee::forSeller).forEach(fee ->
            toTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        status.setRelation(accountChannelService.submit(toTransaction));

        // 卖家佣金存储在TradeOrder订单模型中
        long toFee = fees.stream().filter(Fee::forSeller).mapToLong(Fee::getAmount).sum();
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, null, toFee,
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
            List<PaymentFee> paymentFeeDos = fees.stream().map(fee -> PaymentFee.of(paymentId, fee.getUseFor(),
                fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
            ).collect(Collectors.toList());
            paymentFeeDao.insertPaymentFees(paymentFeeDos);
        }

        // 生成交易双方的业务账单
        List<UserStatement> statements = new ArrayList<>(2);
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
            .channelId(paymentDo.getChannelId()).accountId(paymentDo.getAccountId(), fromAccount.getParentId())
            .type(StatementType.TRADE.getCode()).typeName(StatementType.TRADE.getName())
            .amount(- (paymentDo.getAmount() + fromFee)).fee(fromFee).balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).collect(statements);
        TransactionStatus relation = status.getRelation();
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentDo.getPaymentId())
            .channelId(paymentDo.getChannelId()).accountId(trade.getAccountId(), toAccount.getParentId())
            .type(StatementType.TRADE.getCode()).typeName(StatementType.TRADE.getName())
            .amount(paymentDo.getAmount() - toFee).fee(toFee).balance(relation.getBalance() + relation.getAmount())
            .frozenAmount(relation.getFrozenBalance() + relation.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).collect(statements);
        userStatementDao.insertUserStatements(DataPartition.strategy(fromAccount.getMchId()), statements);

        // 处理商户收益 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        if (!fees.isEmpty()) {
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction merTransaction = merChannel.openTransaction(trade.getType(), now);
            fees.forEach(fee -> merTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
            accountChannelService.submitExclusively(merTransaction);
        }

        // 刷新风控数据
        riskControlEngine.admitPassport(passport);
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * 撤销交易-退交易资金和佣金，交易撤销需要修改交易订单状态
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        if (trade.getState() != TradeState.SUCCESS.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效的交易状态，不能进行撤销操作");
        }

        // "即时交易"业务不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));

        // 撤销交易，需验证退款方账户状态无须验证密码
        LocalDateTime now = LocalDateTime.now().withNano(0);
        UserAccount fromAccount = accountChannelService.checkTradePermission(trade.getAccountId());
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());

        // 处理卖家退款和退佣金，由于底层先产生收入明细后产生支出明细(FundActivity.compare)
        // 这样保证卖家先退款后收入佣金不会造成收支明细中期初余额出现负数（资金仍然是安全的）
        List<PaymentFee> fees = paymentFeeDao.findPaymentFees(payment.getPaymentId());
        AccountChannel fromChannel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
        fromTransaction.outgo(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        fees.stream().filter(PaymentFee::forSeller).forEach(fee ->
            fromTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 处理买家收款和退佣金
        UserAccount toAccount = fundAccountService.findUserAccountById(payment.getAccountId());
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        AccountChannel toChannel = AccountChannel.of(paymentId, toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
        fees.stream().filter(PaymentFee::forBuyer)
            .forEach(fee -> toTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        toTransaction.income(trade.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        status.setRelation(accountChannelService.submit(toTransaction));

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

        // 生成交易双方的业务账单
        List<UserStatement> statements = new ArrayList<>(2);
        TransactionStatus relation = status.getRelation();
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId).channelId(payment.getChannelId())
            .accountId(payment.getAccountId(), toAccount.getParentId()).type(StatementType.REFUND.getCode())
            .typeName(StatementType.TRADE.getName() + "-" +StatementType.REFUND.getName())
            .amount(payment.getAmount() + payment.getFee()).fee(payment.getFee())
            .balance(relation.getBalance() + relation.getAmount())
            .frozenAmount(relation.getFrozenBalance() + relation.getFrozenAmount())
            .serialNo(trade.getSerialNo()).state(4).createdTime(now).collect(statements);
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId).channelId(payment.getChannelId())
            .accountId(trade.getAccountId(), fromAccount.getParentId()).type(StatementType.REFUND.getCode())
            .typeName(StatementType.TRADE.getName() + "-" +StatementType.REFUND.getName())
            .amount(- payment.getAmount() + trade.getFee()).fee(trade.getFee())
            .balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount())
            .serialNo(trade.getSerialNo()).state(4).createdTime(now).collect(statements);
        userStatementDao.insertUserStatements(DataPartition.strategy(fromAccount.getMchId()), statements);

        // 处理商户退佣金 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        if (!fees.isEmpty()) {
            MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
            AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
            IFundTransaction merTransaction = merChannel.openTransaction(TradeType.CANCEL_TRADE.getCode(), now);
            fees.forEach(fee -> merTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
            accountChannelService.submitExclusively(merTransaction);
        }
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    @Override
    public TradeType supportType() {
        return TradeType.DIRECT_TRADE;
    }
}
