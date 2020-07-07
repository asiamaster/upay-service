package com.diligrp.xtrade.upay.channel.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 资金冻结订单数据模型
 */
public class FrozenOrder extends BaseDo {
    // 冻结ID
    private Long frozenId;
    // 支付ID
    private String paymentId;
    // 账号ID
    private Long accountId;
    // 业务账号
    private Long businessId;
    // 用户名
    private String name;
    // 冻结类型-系统冻结 交易冻结
    private Integer type;
    // 金额-分
    private Long amount;
    // 状态-冻结 解冻
    private Integer state;
    // 备注
    private String description;
    // 数据版本号
    private Integer version;

    public Long getFrozenId() {
        return frozenId;
    }

    public void setFrozenId(Long frozenId) {
        this.frozenId = frozenId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static Builder builder() {
        return new FrozenOrder().new Builder();
    }

    public class Builder {
        public Builder frozenId(Long frozenId) {
            FrozenOrder.this.frozenId = frozenId;
            return this;
        }

        public Builder paymentId(String paymentId) {
            FrozenOrder.this.paymentId = paymentId;
            return this;
        }

        public Builder accountId(Long accountId) {
            FrozenOrder.this.accountId = accountId;
            return this;
        }

        public Builder businessId(Long businessId) {
            FrozenOrder.this.businessId = businessId;
            return this;
        }

        public Builder name(String name) {
            FrozenOrder.this.name = name;
            return this;
        }

        public Builder type(Integer type) {
            FrozenOrder.this.type = type;
            return this;
        }

        public Builder amount(Long amount) {
            FrozenOrder.this.amount = amount;
            return this;
        }

        public Builder state(Integer state) {
            FrozenOrder.this.state = state;
            return this;
        }

        public Builder description(String description) {
            FrozenOrder.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            FrozenOrder.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            FrozenOrder.this.createdTime = createdTime;
            return this;
        }

        public FrozenOrder build() {
            return FrozenOrder.this;
        }
    }
}
