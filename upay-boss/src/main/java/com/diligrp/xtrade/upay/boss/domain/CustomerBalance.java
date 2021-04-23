package com.diligrp.xtrade.upay.boss.domain;

import java.util.List;

/**
 * 客户资金余额信息模型
 *
 * @author: brenthuang
 * @date: 2020/12/22
 */
public class CustomerBalance {
    // 客户ID
    private Long customerId;
    // 总余额-分
    private Long balance;
    // 总冻结金额-分
    private Long frozenAmount;
    // 账户资金明细
    private List<FundBalance> fundAccounts;

    public static CustomerBalance of(Long customerId, Long balance, Long frozenAmount, List<FundBalance> fundAccounts) {
        CustomerBalance customer = new CustomerBalance();
        customer.setCustomerId(customerId);
        customer.setBalance(balance);
        customer.setFrozenAmount(frozenAmount);
        customer.setFundAccounts(fundAccounts);
        return customer;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
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

    public List<FundBalance> getFundAccounts() {
        return fundAccounts;
    }

    public void setFundAccounts(List<FundBalance> fundAccounts) {
        this.fundAccounts = fundAccounts;
    }
}
