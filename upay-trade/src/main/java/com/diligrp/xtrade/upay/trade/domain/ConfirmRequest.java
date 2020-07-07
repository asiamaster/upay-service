package com.diligrp.xtrade.upay.trade.domain;

import java.util.List;
import java.util.Optional;

/**
 * 预授权交易确认消费请求模型
 */
public class ConfirmRequest {
    // 交易ID
    private String tradeId;
    // 交易账户ID
    private Long accountId;
    // 确认消费金额
    private Long amount;
    // 支付密码
    private String password;
    // 缴费
    private List<Fee> fees;

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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Fee> getFees() {
        return fees;
    }

    public void setFees(List<Fee> fees) {
        this.fees = fees;
    }

    public Optional<List<Fee>> fees() {
        return fees != null && fees.size() > 0 ? Optional.of(fees) : Optional.empty();
    }
}