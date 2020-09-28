package com.diligrp.xtrade.upay.core.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;
import java.util.function.Consumer;

/**
 * 资金账户数据模型
 */
public class FundAccount extends BaseDo {
    // 客户ID
    private Long customerId;
    // 账号ID
    private Long accountId;
    // 父账号ID
    private Long parentId;
    // 账号类型
    private Integer type;
    // 业务用途
    private Integer useFor;
    // 账户权限
    private Integer permission;
    // 用户名
    private String name;
    // 性别
    private Integer gender;
    // 手机号
    private String mobile;
    // 邮箱地址
    private String email;
    // 证件类型
    private Integer idType;
    // 证件号码
    private String idCode;
    // 联系地址
    private String address;
    // 交易密码
    private String password;
    // 安全密钥
    private String secretKey;
    // 账号状态
    private Integer state;
    // 商户ID
    private Long mchId;
    // 数据版本号
    private Integer version;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public Integer getPermission() {
        return permission;
    }

    public void setPermission(Integer permission) {
        this.permission = permission;
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

    public Integer getIdType() {
        return idType;
    }

    public void setIdType(Integer idType) {
        this.idType = idType;
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

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void ifMasterAccount(Consumer<FundAccount> action) {
        if (parentId == 0) {
            action.accept(this);
        }
    }

    public void ifChildAccount(Consumer<FundAccount> action) {
        if (parentId != 0) {
            action.accept(this);
        }
    }

    public static Builder builder() {
        return new FundAccount().new Builder();
    }

    public class Builder {
       public Builder customerId(Long customerId) {
           FundAccount.this.customerId = customerId;
           return this;
       }

        public Builder accountId(Long accountId) {
            FundAccount.this.accountId = accountId;
            return this;
        }

        public Builder parentId(Long parentId) {
            FundAccount.this.parentId = parentId;
            return this;
        }

        public Builder type(Integer type) {
            FundAccount.this.type = type;
            return this;
        }

        public Builder useFor(Integer useFor) {
            FundAccount.this.useFor = useFor;
            return this;
        }

        public Builder permission(Integer permission) {
           FundAccount.this.permission = permission;
           return this;
        }

        public Builder name(String name) {
            FundAccount.this.name = name;
            return this;
        }

        public Builder gender(Integer gender) {
            FundAccount.this.gender = gender;
            return this;
        }

        public Builder mobile(String mobile) {
            FundAccount.this.mobile = mobile;
            return this;
        }

        public Builder email(String email) {
            FundAccount.this.email = email;
            return this;
        }

        public Builder idType(Integer idType) {
           FundAccount.this.idType = idType;
           return this;
        }

        public Builder idCode(String idCode) {
            FundAccount.this.idCode = idCode;
            return this;
        }

        public Builder address(String address) {
            FundAccount.this.address = address;
            return this;
        }

        public Builder password(String password) {
            FundAccount.this.password = password;
            return this;
        }

        public Builder secretKey(String secretKey) {
            FundAccount.this.secretKey = secretKey;
            return this;
        }

        public Builder state(Integer state) {
            FundAccount.this.state = state;
            return this;
        }

        public Builder mchId(Long mchId) {
            FundAccount.this.mchId = mchId;
            return this;
        }

        public Builder version(Integer version) {
            FundAccount.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            FundAccount.this.createdTime = createdTime;
            return this;
        }

        public FundAccount build() {
            return FundAccount.this;
        }
    }
}
