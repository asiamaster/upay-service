package com.diligrp.xtrade.upay.core.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 接入商户数据模型
 */
public class Merchant extends BaseDo {
    // 商户ID
    private Long mchId;
    // 商户编码
    private String code;
    // 商户名称
    private String name;
    // 收益账户
    private Long profitAccount;
    // 担保账户
    private Long vouchAccount;
    // 押金账户
    private Long pledgeAccount;
    // 商户地址
    private String address;
    // 联系人
    private String contact;
    // 手机号
    private String mobile;
    // 商户私钥
    private String privateKey;
    // 商户公钥
    private String publicKey;
    // 商户状态
    private Integer state;

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

    public Long getProfitAccount() {
        return profitAccount;
    }

    public void setProfitAccount(Long profitAccount) {
        this.profitAccount = profitAccount;
    }

    public Long getVouchAccount() {
        return vouchAccount;
    }

    public void setVouchAccount(Long vouchAccount) {
        this.vouchAccount = vouchAccount;
    }

    public Long getPledgeAccount() {
        return pledgeAccount;
    }

    public void setPledgeAccount(Long pledgeAccount) {
        this.pledgeAccount = pledgeAccount;
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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public static Builder builder() {
        return new Merchant().new Builder();
    }

    public class Builder {
        public Builder mchId(Long mchId) {
            Merchant.this.mchId = mchId;
            return this;
        }

        public Builder code(String code) {
            Merchant.this.code = code;
            return this;
        }

        public Builder name(String name) {
            Merchant.this.name = name;
            return this;
        }

        public Builder profitAccount(Long profitAccount) {
            Merchant.this.profitAccount = profitAccount;
            return this;
        }

        public Builder vouchAccount(Long vouchAccount) {
            Merchant.this.vouchAccount = vouchAccount;
            return this;
        }

        public Builder pledgeAccount(Long pledgeAccount) {
            Merchant.this.pledgeAccount = pledgeAccount;
            return this;
        }

        public Builder address(String address) {
            Merchant.this.address = address;
            return this;
        }

        public Builder contact(String contact) {
            Merchant.this.contact = contact;
            return this;
        }

        public Builder mobile(String mobile) {
            Merchant.this.mobile = mobile;
            return this;
        }

        public Builder privateKey(String privateKey) {
            Merchant.this.privateKey = privateKey;
            return this;
        }

        public Builder publicKey(String publicKey) {
            Merchant.this.publicKey = publicKey;
            return this;
        }

        public Builder state(Integer state) {
            Merchant.this.state = state;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            Merchant.this.createdTime = createdTime;
            return this;
        }

        public Merchant build() {
            return Merchant.this;
        }
    }
}
