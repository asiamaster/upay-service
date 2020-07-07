package com.diligrp.xtrade.upay.trade.domain;

/**
 * 交易退款申请，包括：交易撤销、交易退款和交易冲正
 */
public class RefundRequest {
    // 原交易ID
    private String tradeId;
    // 交易账户ID
    private Long accountId;
    // 处理金额
    private Long amount;
    // 支付密码
    private String password;

    public static RefundRequest of(String tradeId, Long accountId, Long amount, String password) {
        RefundRequest refund = new RefundRequest();
        refund.setTradeId(tradeId);
        refund.setAccountId(accountId);
        refund.setAmount(amount);
        refund.setPassword(password);
        return refund;
    }

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
}
