package com.diligrp.xtrade.upay.trade.domain;

/**
 * 渠道信息模型
 *
 * @author: brenthuang
 * @date: 2020/12/11
 */
public class ChannelAccount {
    // 账户号
    private String toAccount;
    // 账户名称
    private String toName;
    // 账户类型
    private Integer toType;
    // 银行联行行号
    private String bankNo;
    // 银行名称
    private String bankName;

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Integer getToType() {
        return toType;
    }

    public void setToType(Integer toType) {
        this.toType = toType;
    }

    public String getBankNo() {
        return bankNo;
    }

    public void setBankNo(String bankNo) {
        this.bankNo = bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
}
