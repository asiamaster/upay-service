package com.diligrp.xtrade.upay.trade.domain;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 支付申请模型
 */
public class PaymentRequest {
    // 交易ID
    private String tradeId;
    // 资金账号ID
    private Long accountId;
    // 支付渠道
    private Integer channelId;
    // 支付密码
    private String password;
    // 费用列表
    private List<Fee> fees;
    // 抵扣费用 - 综合收费专用
    private List<Fee> deductFees;
    // 免密协议号
    private Long protocolId;
    // 渠道账户信息
    private ChannelAccount channelAccount;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Fee> getFees() {
        return fees == null ? Collections.EMPTY_LIST : fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    public List<Fee> getDeductFees() {
        return deductFees;
    }

    public void setDeductFees(List<Fee> deductFees) {
        this.deductFees = deductFees;
    }

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
    }

    public ChannelAccount getChannelAccount() {
        return channelAccount;
    }

    public void setChannelAccount(ChannelAccount channelAccount) {
        this.channelAccount = channelAccount;
    }

    public Optional<List<Fee>> fees() {
        return fees != null && fees.size() > 0 ? Optional.of(fees) : Optional.empty();
    }

    public Optional<List<Fee>> deductFees() {
        return deductFees != null && deductFees.size() > 0 ? Optional.of(deductFees) : Optional.empty();
    }

    public Optional<ChannelAccount> channelAccount() {
        return Optional.ofNullable(channelAccount);
    }
}
