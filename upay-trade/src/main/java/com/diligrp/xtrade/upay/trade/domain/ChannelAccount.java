package com.diligrp.xtrade.upay.trade.domain;

/**
 * 渠道信息模型
 *
 * @author: brenthuang
 * @date: 2020/12/11
 */
public class ChannelAccount {
    // 账户号
    private String accountNo;
    // 账户名称
    private String accountName;
    // 账户类型
    private Integer type;

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
