package com.diligrp.xtrade.upay.boss.domain;

/**
 * 账户余额信息模型
 */
public class FundBalance {
    // 账号ID
    private Long accountId;
    // 账户余额-分
    private Long balance;
    // 冻结金额-分
    private Long frozenAmount;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(Long frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public Long getAvailableAmount() {
        return balance - frozenAmount;
    }

    public static FundBalance of(Long accountId, Long balance, Long frozenAmount) {
        FundBalance fundBalance = new FundBalance();
        fundBalance.setAccountId(accountId);
        fundBalance.setBalance(balance);
        fundBalance.setFrozenAmount(frozenAmount);
        return fundBalance;
    }
}
