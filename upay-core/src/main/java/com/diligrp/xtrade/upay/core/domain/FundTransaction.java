package com.diligrp.xtrade.upay.core.domain;

import java.time.LocalDateTime;

/**
 * 资金事务模型
 */
public class FundTransaction {
    // 支付ID
    private String paymentId;
    // 资金账号ID
    private long accountId;
    // 业务账号ID - 可为NULL
    private Long businessId;
    // 业务类型
    private int type;
    // 冻结或解冻金额
    private long frozenAmount;
    // 资金明细
    private FundActivity[] activities;
    // 发生时间
    private LocalDateTime when;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(long frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public FundActivity[] getActivities() {
        return activities;
    }

    public void setActivities(FundActivity[] activities) {
        this.activities = activities;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public void setWhen(LocalDateTime when) {
        this.when = when;
    }

    public boolean isFrozenTransacton() {
        return frozenAmount > 0;
    }

    public boolean isUnfrozenTransaction() {
        return frozenAmount < 0;
    }

    public boolean isFundTransaction() {
        return activities != null && activities.length > 0;
    }

    public static FundTransaction of(String paymentId, long accountId, Long businessId, int type, long frozenAmount,
                                     FundActivity[] activities, LocalDateTime when) {
        FundTransaction transaction = new FundTransaction();
        transaction.setPaymentId(paymentId);
        transaction.setAccountId(accountId);
        transaction.setBusinessId(businessId);
        transaction.setType(type);
        transaction.setFrozenAmount(frozenAmount);
        transaction.setActivities(activities);
        transaction.setWhen(when);
        return transaction;
    }
}
