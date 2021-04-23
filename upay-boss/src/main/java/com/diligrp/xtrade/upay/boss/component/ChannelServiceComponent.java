package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.BankCard;
import com.diligrp.xtrade.upay.boss.domain.BankName;
import com.diligrp.xtrade.upay.boss.domain.MerchantId;
import com.diligrp.xtrade.upay.channel.domain.BankChannel;
import com.diligrp.xtrade.upay.channel.domain.Channel;
import com.diligrp.xtrade.upay.channel.service.IPaymentChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineStatementQuery;
import com.diligrp.xtrade.upay.pipeline.domain.UserPipelineStatement;
import com.diligrp.xtrade.upay.trade.service.IPipelinePaymentProcessor;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 支付通道服务组件
 */
@CallableComponent(id = "payment.channel.service")
public class ChannelServiceComponent {

    @Resource
    private IPipelinePaymentProcessor pipelinePaymentProcessor;

    @Resource
    private IPaymentChannelService paymentChannelService;

    /**
     * 分页查询当日客户通道支付记录
     */
    public PageMessage<UserPipelineStatement> listTrades(ServiceRequest<PipelineStatementQuery> request) {
        PipelineStatementQuery query = request.getData();
        AssertUtils.notNull(query.getType(), "type missed");
        AssertUtils.notNull(query.getStartDate(), "startDate missed");
        AssertUtils.notNull(query.getPageNo(), "pageNo missed");
        AssertUtils.notNull(query.getPageSize(), "pageSize missed");
        AssertUtils.isTrue(query.getPageNo() > 0, "invalid pageNo");
        AssertUtils.isTrue(query.getPageSize() > 0, "invalid pageSize");
        LocalDate endDate = query.getEndDate() != null ? query.getEndDate().plusDays(1) : query.getEndDate();
        query.setEndDate(endDate);
        query.from(query.getPageNo(), query.getPageSize());

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        query.setMchId(permit.getMerchant().getMchId());
        return pipelinePaymentProcessor.listPipelineStatements(query);
    }

    /**
     * 获取商户支持的支付渠道
     */
    public List<Channel> listChannels(ServiceRequest<MerchantId> request) {
        MerchantId merchant = request.getData();
        AssertUtils.notNull(merchant.getMchId(), "mchId missed");

        return paymentChannelService.supportedChannels(merchant.getMchId()).stream().map(type ->
            Channel.of(type.getCode(), type.getName())).collect(Collectors.toList());
    }

    /**
     * 根据银行卡号获取该商户允许的支付渠道
     * 如果银行卡对应的银行渠道不在商户支付渠道列表中则抛出异常
     */
    public Channel bankCard(ServiceRequest<BankCard> request) {
        BankCard card = request.getData();
        AssertUtils.notEmpty(card.getCardNo(), "cardNo missed");

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        ChannelType type = paymentChannelService.bankChannelByCardNo(permit.getMerchant().getMchId(), card.getCardNo());
        return Channel.of(type.getCode(), type.getName());
    }

    public List<BankChannel> listBanks(ServiceRequest<BankName> request) {
        BankName bank = request.getData();
        AssertUtils.notEmpty(bank.getBankName(), "bankName missed");
        return paymentChannelService.listBankChannelByName(bank.getBankName());
    }
}
