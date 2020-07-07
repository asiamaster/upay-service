package com.diligrp.xtrade.upay.core.domain;

/**
 * 账号注册请求模型
 */
public class RegisterAccount {
    // 客户ID
    private Long customerId;
    // 账号类型
    private Integer type;
    // 业务用途
    private Integer useFor;
    // 登陆账号-卡号
    private String code;
    // 姓名
    private String name;
    // 性别
    private Integer gender;
    // 手机号
    private String mobile;
    // 邮箱
    private String email;
    // 身份证编号
    private String idCode;
    // 地址
    private String address;
    // 交易密码
    private String password;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getUseFor() {
        return useFor;
    }

    public void setUseFor(Integer useFor) {
        this.useFor = useFor;
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

    public Integer getGender() {
        return gender;
    }

    public void setGender(Integer gender) {
        this.gender = gender;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIdCode() {
        return idCode;
    }

    public void setIdCode(String idCode) {
        this.idCode = idCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static Builder builder() {
        return new RegisterAccount().new Builder();
    }

    public class Builder {
        public Builder customerId(Long customerId) {
            RegisterAccount.this.customerId = customerId;
            return this;
        }

        public Builder type(Integer type) {
            RegisterAccount.this.type = type;
            return this;
        }

        public Builder useFor(Integer useFor) {
            RegisterAccount.this.useFor = useFor;
            return this;
        }

        public Builder code(String code) {
            RegisterAccount.this.code = code;
            return this;
        }

        public Builder name(String name) {
            RegisterAccount.this.name = name;
            return this;
        }

        public Builder gender(Integer gender) {
            RegisterAccount.this.gender = gender;
            return this;
        }

        public Builder mobile(String mobile) {
            RegisterAccount.this.mobile = mobile;
            return this;
        }

        public Builder email(String email) {
            RegisterAccount.this.email = email;
            return this;
        }

        public Builder idCode(String idCode) {
            RegisterAccount.this.idCode = idCode;
            return this;
        }

        public Builder address(String address) {
            RegisterAccount.this.address = address;
            return this;
        }

        public Builder password(String password) {
            RegisterAccount.this.password = password;
            return this;
        }

        public RegisterAccount build() {
            return RegisterAccount.this;
        }
    }
}
