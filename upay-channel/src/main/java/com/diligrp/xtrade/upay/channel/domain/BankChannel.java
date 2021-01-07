package com.diligrp.xtrade.upay.channel.domain;

/**
 * 银行通道模型
 *
 * @author: brenthuang
 * @date: 2021/01/06
 */
public class BankChannel {
    // 银行编码 - SJBANK, CCB等
    private String code;
    // 银行联行行号
    private String bankNo;
    // 银行名称
    private String bankName;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
