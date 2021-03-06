package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.domain.*;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.service.IPaymentPlatformService;
import com.diligrp.xtrade.upay.trade.service.IPaymentService;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import com.diligrp.xtrade.upay.trade.type.TradeType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 支付平台服务：聚合所有支持的业务类型，根据不同的交易类型将请求分发只不同的业务服务组件
 */
@Service("paymentPlatformService")
public class PaymentPlatformServiceImpl implements IPaymentPlatformService, BeanPostProcessor {

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    private Map<TradeType, IPaymentService> services = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String createTrade(ApplicationPermit application, TradeRequest trade) {
        LocalDateTime when = LocalDateTime.now();
        Optional<TradeType> tradeType = TradeType.getType(trade.getType());
        tradeType.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));
        UserAccount account = fundAccountService.findUserAccountById(trade.getAccountId());
        accountChannelService.checkAccountTradeState(account);
        // 收益商户只能是账户所属商户，或账户所属商户的子商户(排除accountId=0的特殊账号)
        MerchantPermit merchant = application.getMerchant();
        if (account.getMchId() != 0 && !ObjectUtils.equals(account.getMchId(), merchant.getMchId()) &&
            !ObjectUtils.equals(account.getMchId(), merchant.getParentId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "不合法的收益商户");
        }

        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.TRADE_ID);
        String tradeId = String.valueOf(keyGenerator.nextId());
        TradeOrder tradeOrder = TradeOrder.builder().mchId(application.getMerchant().getMchId()).appId(application.getAppId())
            .tradeId(tradeId).type(trade.getType()).serialNo(trade.getSerialNo()).cycleNo(trade.getCycleNo())
            .accountId(account.getAccountId()).name(account.getName())
            .amount(trade.getAmount()).maxAmount(trade.getAmount()).fee(0L).state(TradeState.PENDING.getCode())
            .description(trade.getDescription()).version(0).createdTime(when).build();
        tradeOrderDao.insertTradeOrder(tradeOrder);
        return tradeId;
    }

    /**
     * {@inheritDoc}
     *
     * 预授权业务只冻结资金不进行实际交易
     */
    @Override
    public PaymentResult commit(ApplicationPermit application, PaymentRequest request) {
        Optional<ChannelType> channelType = ChannelType.getType(request.getChannelId());
        channelType.orElseThrow(() -> new TradePaymentException(ErrorCode.CHANNEL_NOT_SUPPORTED, "不支持的支付渠道"));

        Optional<TradeOrder> tradeOpt = tradeOrderDao.findTradeOrderById(request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (trade.getState() != TradeState.PENDING.getCode()) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "无效的交易状态");
        }
        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));
        Optional<IPaymentService> serviceOpt = tradeService(tradeType);
        IPaymentService service = serviceOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));

        // 检查是否是系统支持的费用类型 - 支付系统只负责记录费用类型，暂不校验费用类型，原因是业务系统费用过多且可动态配置
//        List<Fee> feeList = request.fees().orElseGet(Collections::emptyList);
//        feeList.stream().map(fee -> FundType.getFee(fee.getType())).forEach(feeOpt ->
//            feeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的费用类型")));

        Payment payment = Payment.of(request.getAccountId(), trade.getAmount(), request.getChannelId(),
            request.getPassword(), request.getProtocolId());
        payment.put(MerchantPermit.class.getName(), application.getMerchant());
        request.fees().ifPresent(fees -> payment.put(Fee.class.getName(), fees));
        request.deductFees().ifPresent(fees -> payment.put(Fee.class.getName() + ".deduct", fees));
        request.channelAccount().ifPresent(channelAccount -> payment.put(ChannelAccount.class.getName(), channelAccount));
        return service.commit(trade, payment);
    }

    /**
     * {@inheritDoc}
     *
     * 预授权业务确认交易，解冻资金并实际发生资金交易
     */
    @Override
    public PaymentResult confirm(ApplicationPermit application, ConfirmRequest request) {
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findTradeOrderById(request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forConfirm(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "无效的交易状态，不能确认消费");
        }
        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));
        Optional<IPaymentService> serviceOpt = tradeService(tradeType);
        IPaymentService service = serviceOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));

        Confirm confirm = Confirm.of(request.getAccountId(), request.getAmount(), request.getPassword());
        confirm.put(MerchantPermit.class.getName(), application.getMerchant());
        request.fees().ifPresent(fees -> confirm.put(Fee.class.getName(), fees));
        return service.confirm(trade, confirm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaymentResult refund(ApplicationPermit application, RefundRequest request) {
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findTradeOrderById(request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forRefund(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "无效的交易状态，不能进行交易退款");
        }
        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));
        Optional<IPaymentService> serviceOpt = tradeService(tradeType);
        IPaymentService service = serviceOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));

        Refund refund = Refund.of(trade.getTradeId(), request.getAmount());
        refund.put(MerchantPermit.class.getName(), application.getMerchant());
        request.fees().ifPresent(fees -> refund.put(Fee.class.getName(), fees));
        request.deductFees().ifPresent(fees -> refund.put(Fee.class.getName() + ".deduct", fees));
        return service.refund(trade, refund);
    }

    /**
     * {@inheritDoc}
     *
     * 正常业务撤销将对资金进行逆向操作；对于预授权业务，确认交易前撤销只解冻资金，确认交易后撤销进行资金逆向操作
     */
    @Override
    public PaymentResult cancel(ApplicationPermit application, RefundRequest request) {
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findTradeOrderById(request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forCancel(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "无效的交易状态，不能撤销交易");
        }
        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));
        Optional<IPaymentService> serviceOpt = tradeService(tradeType);
        IPaymentService service = serviceOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));

        Refund cancel = Refund.of(trade.getTradeId(), trade.getAmount());
        cancel.put(MerchantPermit.class.getName(), application.getMerchant());
        return service.cancel(trade, cancel);
    }

    /**
     * {@inheritDoc}
     *
     * 目前只有充值、提现允许进行交易冲正
     */
    @Override
    public PaymentResult correct(ApplicationPermit application, CorrectRequest request) {
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findTradeOrderById(request.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_FOUND, "交易不存在"));
        checkTradePermission(trade, application.getMerchant());
        if (!TradeState.forCorrect(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "无效的交易状态，不能进行交易冲正");
        }
        Optional<TradeType> typeOpt = TradeType.getType(trade.getType());
        TradeType tradeType = typeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));
        Optional<IPaymentService> serviceOpt = tradeService(tradeType);
        IPaymentService service = serviceOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.TRADE_NOT_SUPPORTED, "不支持的交易类型"));

        Correct correct = Correct.of(request.getTradeId(), request.getAccountId(), request.getAmount());
        correct.put(MerchantPermit.class.getName(), application.getMerchant());
        request.fee().ifPresent(fee -> correct.put(Fee.class.getName(), fee));
        return service.correct(trade, correct);
    }

    /**
     * 检查商户是否有权限操作该交易订单: 接口权限商户与交易订单所属商户必须有共同的父商户
     */
    private void checkTradePermission(TradeOrder order, MerchantPermit permit) {
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(order.getMchId());
        if (!ObjectUtils.equals(merchant.parentMchId(), permit.parentMchId())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "商户没有权限操作该交易订单");
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof IPaymentService) {
            IPaymentService tradeService = (IPaymentService) bean;
            services.put(tradeService.supportType(), tradeService);
        }
        return bean;
    }

    private Optional<IPaymentService> tradeService(TradeType tradeType) {
        return Optional.ofNullable(services.get(tradeType));
    }
}
