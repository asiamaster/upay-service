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
    // 交易冻结金额-分
    private Long tradeFrozen;
    // 人工冻结金额-分
    private Long manFrozen;

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

    public Long getTradeFrozen() {
        return tradeFrozen;
    }

    public void setTradeFrozen(Long tradeFrozen) {
        this.tradeFrozen = tradeFrozen;
    }

    public Long getManFrozen() {
        return manFrozen;
    }

    public void setManFrozen(Long manFrozen) {
        this.manFrozen = manFrozen;
    }

    public static FundBalance of(Long accountId, Long balance, Long frozenAmount) {
        FundBalance fundBalance = new FundBalance();
        fundBalance.setAccountId(accountId);
        fundBalance.setBalance(balance);
        fundBalance.setFrozenAmount(frozenAmount);
        return fundBalance;
    }

    public static FundBalance of(Long accountId, Long balance, Long frozenAmount, Long tradeFrozen, Long manFrozen) {
        FundBalance fundBalance = new FundBalance();
        fundBalance.setAccountId(accountId);
        fundBalance.setBalance(balance);
        fundBalance.setFrozenAmount(frozenAmount);
        fundBalance.setTradeFrozen(tradeFrozen);
        fundBalance.setManFrozen(manFrozen);
        return fundBalance;
    }
}
