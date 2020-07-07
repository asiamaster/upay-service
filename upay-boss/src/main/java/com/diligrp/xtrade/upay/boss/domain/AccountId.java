package com.diligrp.xtrade.upay.boss.domain;

/**
 * 账号注册接口层模型
 */
public class AccountId {
    private Long accountId;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public static AccountId of(Long id) {
        AccountId accountId = new AccountId();
        accountId.setAccountId(id);
        return accountId;
    }
}
