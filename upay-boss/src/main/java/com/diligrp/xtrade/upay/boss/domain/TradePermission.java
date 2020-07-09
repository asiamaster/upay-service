package com.diligrp.xtrade.upay.boss.domain;

/**
 * 账户交易权限模型
 */
public class TradePermission {
    // 资金账号
    private Long accountId;
    // 交易密码
    private String password;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
