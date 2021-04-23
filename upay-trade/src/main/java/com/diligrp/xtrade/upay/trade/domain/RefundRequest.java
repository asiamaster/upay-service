package com.diligrp.xtrade.upay.trade.domain;

import java.util.List;
import java.util.Optional;

/**
 * 交易退款申请，包括：交易撤销、交易退款
 */
public class RefundRequest {
    // 原交易ID
    private String tradeId;
    // 处理金额
    private Long amount;
    // 费用列表 - "综合收费"退款时使用
    private List<Fee> fees;
    // 抵扣费用 - "综合收费"退款时使用
    private List<Fee> deductFees;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public List<Fee> getFees() {
        return fees;
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

    public Optional<List<Fee>> fees() {
        return fees != null && fees.size() > 0 ? Optional.of(fees) : Optional.empty();
    }

    public Optional<List<Fee>> deductFees() {
        return deductFees != null && deductFees.size() > 0 ? Optional.of(deductFees) : Optional.empty();
    }
}
