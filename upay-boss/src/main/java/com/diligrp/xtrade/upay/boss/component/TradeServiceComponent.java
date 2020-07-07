package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.TradeId;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.trade.domain.ConfirmRequest;
import com.diligrp.xtrade.upay.trade.domain.PaymentRequest;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.domain.RefundRequest;
import com.diligrp.xtrade.upay.trade.domain.TradeRequest;
import com.diligrp.xtrade.upay.trade.service.IPaymentPlatformService;

import javax.annotation.Resource;

/**
 * 交易服务组件
 */
@CallableComponent(id = "payment.trade.service")
public class TradeServiceComponent {

    @Resource
    private IPaymentPlatformService paymentPlatformService;

    /**
     * 创建交易订单，适用于所有交易业务
     * @see com.diligrp.xtrade.upay.trade.type.TradeType
     */
    public TradeId prepare(ServiceRequest<TradeRequest> request) {
        TradeRequest trade = request.getData();
        // 基本参数校验
        AssertUtils.notNull(trade.getType(), "type missed");
        AssertUtils.notNull(trade.getAccountId(), "accountId missed");
        AssertUtils.notNull(trade.getBusinessId(), "businessId missed");
        AssertUtils.notNull(trade.getAmount(), "amount missed");
        AssertUtils.isTrue(trade.getAmount() > 0, "Invalid amount");

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class.getName(), ApplicationPermit.class);
        String tradeId = paymentPlatformService.createTrade(application, trade);
        return TradeId.of(tradeId);
    }

    /**
     * 交易订单提交支付，适用于所有交易业务
     * 预授权交易提交支付时只是冻结资金，后续需要进一步调用confirm/cancel进行资金操作
     * @see com.diligrp.xtrade.upay.trade.type.TradeType
     */
    public TransactionStatus commit(ServiceRequest<PaymentRequest> request) {
        PaymentRequest payment = request.getData();
        // 基本参数校验
        AssertUtils.notNull(payment.getTradeId(), "tradeId missed");
        AssertUtils.notNull(payment.getAccountId(), "accountId missed");
        AssertUtils.notNull(payment.getBusinessId(), "businessId missed");
        AssertUtils.notNull(payment.getChannelId(), "channelId missed");
        // 费用参数校验
        payment.fees().ifPresent(fees -> fees.stream().forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class.getName(), ApplicationPermit.class);
        PaymentResult result = paymentPlatformService.commit(application, payment);
        // 如有余额信息则返回余额信息
        return result.getStatus();
    }

    /**
     * 确认交易，只适用于预授权交易
     * 预授权交易需经历 prepare->commit->confirm/cancel三个阶段
     * confirm阶段解冻资金并完成实际交易消费，实际交易金额可以大于冻结金额（原订单金额）
     */
    public TransactionStatus confirm(ServiceRequest<ConfirmRequest> request) {
        ConfirmRequest confirm = request.getData();
        AssertUtils.notEmpty(confirm.getTradeId(), "tradeId missed");
        AssertUtils.notNull(confirm.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(confirm.getPassword(), "password missed");
        AssertUtils.notNull(confirm.getAmount(), "amount missed");
        AssertUtils.isTrue(confirm.getAmount() > 0, "Invalid amount");
        // 费用参数校验
        confirm.fees().ifPresent(fees -> fees.stream().forEach(fee -> {
            AssertUtils.notNull(fee.getType(), "fee type missed");
            AssertUtils.notNull(fee.getTypeName(), "fee name missed");
            AssertUtils.notNull(fee.getAmount(), "fee amount missed");
            AssertUtils.isTrue(fee.getAmount() > 0, "Invalid fee amount");
        }));

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class.getName(), ApplicationPermit.class);
        PaymentResult result = paymentPlatformService.confirm(application, confirm);
        // 如有余额信息则返回余额信息
        return result.getStatus();
    }

    /**
     * 取消交易，适用于普通交易和预授权交易类型
     * 预授权交易需经历 prepare->commit->confirm/cancel三个阶段
     * 预授权交易的cancel阶段完成资金解冻，不进行任何消费；交易确认后cancel将完成资金逆向操作
     */
    public TransactionStatus cancel(ServiceRequest<RefundRequest> request) {
        RefundRequest cancel = request.getData();
        AssertUtils.notEmpty(cancel.getTradeId(), "tradeId missed");
        AssertUtils.notNull(cancel.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(cancel.getPassword(), "password missed");

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class.getName(), ApplicationPermit.class);
        PaymentResult result = paymentPlatformService.cancel(application, cancel);
        // 如有余额信息则返回余额信息
        return result.getStatus();
    }
}