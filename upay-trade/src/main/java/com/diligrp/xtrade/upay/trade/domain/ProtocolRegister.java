package com.diligrp.xtrade.upay.trade.domain;

/**
 * 注册免密支付协议业务模型
 *
 * @author: brenthuang
 * @date: 2020/10/10
 */
public class ProtocolRegister {
    // 协议类型
    private Integer type;
    // 账号ID
    private Long accountId;
    // 交易密码
    private String password;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

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
