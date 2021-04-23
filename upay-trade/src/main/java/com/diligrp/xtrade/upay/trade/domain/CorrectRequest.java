package com.diligrp.xtrade.upay.trade.domain;

import java.util.Optional;

/**
 * 交易冲正申请: 目前只有充值提现会进行冲正操作
 *
 * @author: brenthuang
 * @date: 2020/12/02
 */
public class CorrectRequest {
    // 原交易号
    private String tradeId;
    // 交易账户ID
    private Long accountId;
    // 冲正金额
    private Long amount;
    // 冲正费用
    private Fee fee;

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

    public Fee getFee() {
        return fee;
    }

    public void setFee(Fee fee) {
        this.fee = fee;
    }

    public Optional<Fee> fee() {
        return Optional.ofNullable(fee);
    }
}
