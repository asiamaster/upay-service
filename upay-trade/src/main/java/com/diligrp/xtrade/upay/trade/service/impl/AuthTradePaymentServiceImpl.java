package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.dao.IUserStatementDao;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.FrozenStateDto;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.channel.model.UserStatement;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.channel.type.FrozenState;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.channel.type.StatementType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.AsyncTaskExecutor;
import com.diligrp.xtrade.upay.sentinel.domain.Passport;
import com.diligrp.xtrade.upay.sentinel.domain.RiskControlEngine;
import com.diligrp.xtrade.upay.sentinel.service.IRiskControlService;
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
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     * 提交预授权交易只冻结资金，不进行实际交易；
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forTrade(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行预授权交易业务");
        }
        if (trade.getAccountId().equals(payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "同一账号不能进行交易");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        feesOpt.ifPresent(fees -> { throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "预授权冻结不支持收取费用"); });

        LocalDateTime now = LocalDateTime.now().withNano(0);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        UserAccount fromAccount = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), maxPwdErrors);
        UserAccount toAccount = fundAccountService.findUserAccountById(trade.getAccountId());
        if (!ObjectUtils.equals(fromAccount.getMchId(), toAccount.getMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不能进行跨商户交易");
        }
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑

        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel channel = AccountChannel.of(paymentId, fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction transaction = channel.openTransaction(FrozenState.FROZEN.getCode(), now);
        transaction.freeze(trade.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);

        // 创建冻结资金订单
        Long masterAccountId = fromAccount.getParentId() == 0 ? fromAccount.getAccountId() : fromAccount.getParentId();
        Long childAccountId = fromAccount.getParentId() == 0 ? null : fromAccount.getAccountId();
        IKeyGenerator frozenKey = keyGeneratorManager.getKeyGenerator(SequenceKey.FROZEN_ID);
        // 异步执行避免Seata回滚造成ID重复
        long frozenId = AsyncTaskExecutor.submit(() -> frozenKey.nextId());
        FrozenOrder frozenOrder = FrozenOrder.builder().frozenId(frozenId).paymentId(paymentId)
            .accountId(masterAccountId).childId(childAccountId).name(fromAccount.getName())
            .type(FrozenType.TRADE_FROZEN.getCode()).amount(trade.getAmount()).state(FrozenState.FROZEN.getCode())
            .description(null).version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);

        // 冻结交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.FROZEN.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        // 生成"处理中"的支付记录
        TradePayment paymentDo = TradePayment.builder().paymentId(paymentId).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(payment.getAccountId())
            .name(fromAccount.getName()).cardNo(null).amount(payment.getAmount()).fee(0L).state(PaymentState.PROCESSING.getCode())
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
        LocalDateTime now = LocalDateTime.now().withNano(0);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        UserAccount fromAccount = accountChannelService.checkTradePermission(payment.getAccountId(), confirm.getPassword(), maxPwdErrors);
        accountChannelService.checkAccountTradeState(fromAccount); // 寿光专用业务逻辑
        // 风控检查
        RiskControlEngine riskControlEngine = riskControlService.loadRiskControlEngine(fromAccount);
        Passport passport = Passport.ofTrade(fromAccount.getAccountId(), fromAccount.getPermission(), payment.getAmount());
        riskControlEngine.checkPassport(passport);

        // 处理买家付款和买家佣金
        AccountChannel fromChannel = AccountChannel.of(payment.getPaymentId(), fromAccount.getAccountId(), fromAccount.getParentId());
        IFundTransaction fromTransaction = fromChannel.openTransaction(trade.getType(), now);
        fromTransaction.unfreeze(frozenOrder.getAmount());
        fromTransaction.outgo(confirm.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        fees.stream().filter(Fee::forBuyer)
            .forEach(fee -> fromTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        TransactionStatus status = accountChannelService.submit(fromTransaction);

        // 处理卖家收款和卖家佣金
        UserAccount toAccount = fundAccountService.findUserAccountById(trade.getAccountId());
        accountChannelService.checkAccountTradeState(toAccount); // 寿光专用业务逻辑
        AccountChannel toChannel = AccountChannel.of(payment.getPaymentId(), toAccount.getAccountId(), toAccount.getParentId());
        IFundTransaction toTransaction = toChannel.openTransaction(trade.getType(), now);
        toTransaction.income(confirm.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName(), null);
        fees.stream().filter(Fee::forSeller)
            .forEach(fee -> toTransaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription()));
        status.setRelation(accountChannelService.submit(toTransaction));

        // 修改冻结订单"已解冻"状态
        FrozenStateDto frozenState = FrozenStateDto.of(frozenOrder.getFrozenId(), FrozenState.UNFROZEN.getCode(),
            frozenOrder.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // 卖家佣金存储在TradeOrder订单模型中
        long toFee = fees.stream().filter(Fee::forSeller).mapToLong(Fee::getAmount).sum();
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), confirm.getAmount(), confirm.getAmount(), toFee,
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
            List<PaymentFee> paymentFeeDos = fees.stream().map(fee -> PaymentFee.of(payment.getPaymentId(),
                fee.getUseFor(), fee.getAmount(), fee.getType(), fee.getTypeName(), fee.getDescription(), now)
            ).collect(Collectors.toList());
            paymentFeeDao.insertPaymentFees(paymentFeeDos);
        }

        // 生成交易双方的业务账单
        List<UserStatement> statements = new ArrayList<>(2);
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(payment.getPaymentId())
            .channelId(payment.getChannelId()).accountId(payment.getAccountId(), fromAccount.getParentId())
            .type(StatementType.TRADE.getCode()).typeName(StatementType.TRADE.getName())
            .amount(- (confirm.getAmount() + fromFee)).fee(fromFee).balance(status.getBalance() + status.getAmount())
            .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).collect(statements);
        TransactionStatus relation = status.getRelation();
        UserStatement.builder().tradeId(trade.getTradeId()).paymentId(payment.getPaymentId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId(), toAccount.getParentId())
            .type(StatementType.TRADE.getCode()).typeName(StatementType.TRADE.getName())
            .amount(confirm.getAmount() - toFee).fee(toFee).balance(relation.getBalance() + relation.getAmount())
            .frozenAmount(relation.getFrozenBalance() + relation.getFrozenAmount()).serialNo(trade.getSerialNo()).state(4)
            .createdTime(now).collect(statements);
        userStatementDao.insertUserStatements(statements);

        // 处理商户收益 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        if (!fees.isEmpty()) {
            AccountChannel merChannel = AccountChannel.of(payment.getPaymentId(), merchant.getProfitAccount(), 0L);
            IFundTransaction merTransaction = merChannel.openTransaction(trade.getType(), now);
            fees.forEach(fee -> merTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName(), null));
            accountChannelService.submitExclusively(merTransaction);
        }

        // 刷新风控数据
        riskControlEngine.admitPassport(passport);
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

        // "预授权交易"不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));

        // 撤销预授权，需验证买方账户状态无须验证密码
        LocalDateTime when = LocalDateTime.now().withNano(0);
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId());
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findFrozenOrderByPaymentId(payment.getPaymentId());
        FrozenOrder order = orderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (order.getState() != FrozenState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无效交易状态，不能执行该操作");
        }

        // 解冻冻结资金
        AccountChannel channel = AccountChannel.of(payment.getPaymentId(), account.getAccountId(), account.getParentId());
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
