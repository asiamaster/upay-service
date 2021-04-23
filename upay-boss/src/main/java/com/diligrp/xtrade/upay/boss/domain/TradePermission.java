package com.diligrp.xtrade.upay.boss.domain;

import java.util.List;

/**
 * 账户交易权限模型
 */
public class TradePermission {
    // 资金账号
    private Long accountId;
    // 交易密码
    private String password;
    // 已有交易权限码
    private List<Integer> permission;
    // 全量权限码
    private List<Option> allPermission;

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

    public List<Integer> getPermission() {
        return permission;
    }

    public void setPermission(List<Integer> permission) {
        this.permission = permission;
    }

    public List<Option> getAllPermission() {
        return allPermission;
    }

    public void setAllPermission(List<Option> allPermission) {
        this.allPermission = allPermission;
    }
}
