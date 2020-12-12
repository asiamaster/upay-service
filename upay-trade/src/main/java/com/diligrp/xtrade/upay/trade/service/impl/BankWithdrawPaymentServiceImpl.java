package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.service.IChannelRouteService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineRequest;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineResponse;
import com.diligrp.xtrade.upay.trade.domain.ChannelAccount;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.service.IBankWithdrawPipelineProcessor;
import com.diligrp.xtrade.upay.trade.service.IPaymentService;
import com.diligrp.xtrade.upay.trade.type.TradeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * 银行圈提业务
 *
 * @author: brenthuang
 * @date: 2020/12/11
 */
@Service("bankWithdrawPaymentService")
public class BankWithdrawPaymentServiceImpl implements IPaymentService {

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IChannelRouteService channelRouteService;

    @Resource
    private IBankWithdrawPipelineProcessor bankWithdrawPipelineProcessor;


    /**
     * {@inheritDoc}
     *
     * 只支持网银渠道，且在商户收益账户中"退回"手续费给客户，而非收取费用; 专为寿光市场处理网银提现发生异常时使用。
     */
    @Override
    @Transactional(propagation = Propagation.NEVER, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        if (!ChannelType.forBankWithdraw(payment.getChannelId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持该渠道进行银行圈提业务");
        }
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "充值资金账号不一致");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        feesOpt.ifPresent(fees -> { throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "银行圈提费用银行接口返回"); });
        ChannelAccount channelAccount = checkChannelAccount(payment);

        // 检查交易权限, 并选择支付通道
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), -1);
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑
        IPipeline pipeline = channelRouteService.selectPaymentPipeline(supportType().getCode(),
            payment.getChannelId(), trade.getAmount());
        // 向通道发起支付请求
        PipelineRequest request = PipelineRequest.of(null, channelAccount.getAccountNo(),
            channelAccount.getType(), payment.getAmount()).attach(trade).attach(payment).attach(account);
        PipelineResponse response = pipeline.sendTradeRequest(request, bankWithdrawPipelineProcessor);

        return PaymentResult.of(PaymentResult.CODE_SUCCESS, response.getPaymentId(), response.getStatus());
    }

    private ChannelAccount checkChannelAccount(Payment payment) {
        Optional<ChannelAccount> channelAccountOpt = payment.getObject(ChannelAccount.class.getName());
        ChannelAccount channelAccount = channelAccountOpt.orElseThrow(
            () -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "channelAccount missed"));
        AssertUtils.notEmpty(channelAccount.getAccountNo(), "channel accountNo missed");
        AssertUtils.notEmpty(channelAccount.getAccountName(), "channel accountName missed");
        AssertUtils.notNull(channelAccount.getType(), "channel accountType missed");
        AssertUtils.isTrue(channelAccount.getType() >= 1 && channelAccount.getType() <= 2,
            "invalid channel accountType");
        return channelAccount;
    }

    @Override
    public TradeType supportType() {
        return TradeType.BANK_WITHDRAW;
    }
}
