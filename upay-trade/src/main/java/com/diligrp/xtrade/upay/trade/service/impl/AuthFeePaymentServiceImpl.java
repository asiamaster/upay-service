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
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.AsyncTaskExecutor;
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
 * 预授权缴费：先对账户资金进行交易冻结，然后解冻并完成缴费或取消交易并解冻资金。实际缴费金额可以大于交易冻结金额。
 * 业务场景：进门先冻结缴费金额（prepare->commit)，出门后解冻并实际缴费（confirm)或取消交易(cancel)
 */
@Service("authFeePaymentService")
public class AuthFeePaymentServiceImpl extends FeePaymentServiceImpl implements IPaymentService {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IUserStatementDao userStatementDao;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     *
     * 预授权缴费将直接冻结资金，不做实际缴费；提交预授权的资金账号需与创建交易的资金账号一致
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forPreAuthFee(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行预授权缴费业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费资金账号不一致");
        }

        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        feesOpt.ifPresent(fees -> { throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "预授权冻结不支持收取费用"); });

        // 冻结资金
        LocalDateTime now = LocalDateTime.now();
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), -1);
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(FrozenState.FROZEN.getCode(), now);
        transaction.freeze(trade.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);

        // 创建冻结资金订单
        Long masterAccountId = account.getParentId() == 0 ? account.getAccountId() : account.getParentId();
        Long childAccountId = account.getParentId() == 0 ? null : account.getAccountId();
        IKeyGenerator frozenKey = keyGeneratorManager.getKeyGenerator(SequenceKey.FROZEN_ID);
        // 异步执行避免Seata回滚造成ID重复
        long frozenId = AsyncTaskExecutor.submit(() -> frozenKey.nextId());
        FrozenOrder frozenOrder = FrozenOrder.builder().frozenId(frozenId).paymentId(paymentId)
            .accountId(masterAccountId).childId(childAccountId).name(account.getName())
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
            .channelId(payment.getChannelId()).accountId(trade.getAccountId())
            .name(trade.getName()).cardNo(null).amount(payment.getAmount()).fee(0L).state(PaymentState.PROCESSING.getCode())
            .description(TradeType.AUTH_FEE.getName()).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, paymentId, status);
    }

    /**
     * {@inheritDoc}
     *
     * "预授权缴费"业务确认预授权消费(交易冻结后确认实际缴费金额)，当前业务场景允许实际缴费金额大于冻结金额
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult confirm(TradeOrder trade, Confirm confirm) {
        Optional<List<Fee>> feesOpt = confirm.getObjects(Fee.class.getName());
        List<Fee> fees = feesOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无收费信息"));
        // 预授权缴费业务允许实际缴费金额大于预授权金额, 确认金额confirm.amount应该等于费用总和
        long totalFee = fees.stream().mapToLong(Fee::getAmount).sum();
        if (totalFee !=  confirm.getAmount()) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "收费金额参数错误");
        }

        // "预授权缴费"不存在组合支付的情况，因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (!payment.getAccountId().equals(confirm.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费资金账号不一致");
        }
        // 查询冻结订单
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findFrozenOrderByPaymentId(payment.getPaymentId());
        FrozenOrder frozenOrder = orderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (frozenOrder.getState() != FrozenState.FROZEN.getCode()) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "无预授权资金记录");
        }

        // 获取商户收益账号信息
        LocalDateTime now = LocalDateTime.now();
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId(), confirm.getPassword(), -1);
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        // 客户账号资金解冻并缴费
        AccountChannel channel = AccountChannel.of(payment.getPaymentId(), account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
        transaction.unfreeze(frozenOrder.getAmount());
        fees.forEach(fee ->
            transaction.outgo(fee.getAmount(), fee.getType(), fee.getTypeName())
        );
        TransactionStatus status = accountChannelService.submit(transaction);

        // 修改冻结订单"已解冻"状态
        FrozenStateDto frozenState = FrozenStateDto.of(frozenOrder.getFrozenId(), FrozenState.UNFROZEN.getCode(),
            frozenOrder.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // "预授权缴费"的交易单中金额修改成"实际缴费金额"，交易订单中max_amount金额为冻结金额
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), totalFee, totalFee,
            TradeState.SUCCESS.getCode(), trade.getVersion(), now);
        if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        // "预授权缴费"的支付单中金额修改成"实际缴费金额"，并存储费用明细
        PaymentStateDto paymentState = PaymentStateDto.of(payment.getPaymentId(), totalFee,
            PaymentState.SUCCESS.getCode(), payment.getVersion(), now);
        if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        List<PaymentFee> paymentFees = fees.stream().map(fee ->
            PaymentFee.of(payment.getPaymentId(), fee.getAmount(), fee.getType(), fee.getTypeName(), now)
        ).collect(Collectors.toList());
        paymentFeeDao.insertPaymentFees(paymentFees);

        // 生成缴费账户的业务账单
        String typeName = StatementType.PAY_FEE.getName() + (ObjectUtils.isNull(trade.getDescription()) ?
            "" : "-" + trade.getDescription());
        UserStatement statement = UserStatement.builder().appId(trade.getAppId()).tradeId(trade.getTradeId())
            .paymentId(payment.getPaymentId()).channelId(payment.getChannelId()).accountId(payment.getAccountId())
            .type(StatementType.PAY_FEE.getCode()).typeName(typeName).amount(-totalFee).fee(0L)
            .balance(status.getBalance() + status.getAmount()).frozenAmount(status.getFrozenBalance() + status.getFrozenAmount())
            .serialNo(trade.getSerialNo()).state(4).createdTime(now).build();
        userStatementDao.insertUserStatement(statement);

        // 园区收益账户收款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
        AccountChannel merChannel = AccountChannel.of(payment.getPaymentId(), merchant.getProfitAccount(), 0L);
        IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
        fees.forEach(fee ->
            feeTransaction.income(fee.getAmount(), fee.getType(), fee.getTypeName())
        );
        accountChannelService.submit(feeTransaction);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, payment.getPaymentId(), status);
    }

    /**
     * {@inheritDoc}
     *
     * "预授权缴费"确认前撤销交易, 将解冻资金冻结并变成可用余额; 确认缴费后撤销交易，将缴费金额退还至缴费账号。
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public PaymentResult cancel(TradeOrder trade, Refund cancel) {
        // 预授权缴费成功则正常撤销"缴费"交易
        if (trade.getState() == TradeState.SUCCESS.getCode()) {
            return super.cancel(trade, cancel);
        }

        // "预授权缴费"不存在组合支付的情况, 因此一个交易订单只对应一条支付记录
        Optional<TradePayment> paymentOpt = tradePaymentDao.findOneTradePayment(trade.getTradeId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (!payment.getAccountId().equals(cancel.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "缴费资金账号不一致");
        }
        // 撤销预授权，需验证缴费账户状态无须验证密码
        LocalDateTime when = LocalDateTime.now();
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
        return TradeType.AUTH_FEE;
    }
}
