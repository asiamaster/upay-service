package com.diligrp.xtrade.upay.core.domain;

/**
 * 商户注册申请模型
 */
public class RegisterMerchant {
    // 商户ID
    private Long mchId;
    // 商户编码
    private String code;
    // 商户名称
    private String name;
    // 商户地址
    private String address;
    // 联系人
    private String contact;
    // 手机号
    private String mobile;
    // 资金账号密码
    private String password;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
