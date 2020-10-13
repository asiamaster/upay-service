package com.diligrp.xtrade.upay.trade.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 免密支付协议数据模型
 *
 * @author: brenthuang
 * @date: 2020/10/10
 */
public class UserProtocol extends BaseDo {
    // 协议ID
    private Long protocolId;
    // 账号ID
    private Long accountId;
    // 用户名
    private String name;
    // 协议类型
    private Integer type;
    // 最小金额-分
    private Long minAmount;
    // 最大金额-分
    private Long maxAmount;
    // 生效时间
    private LocalDateTime startOn;
    // 协议状态
    private Integer state;
    // 备注
    private String description;
    // 数据版本号
    private Integer version;

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public Long getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Long minAmount) {
        this.minAmount = minAmount;
    }

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public LocalDateTime getStartOn() {
        return startOn;
    }

    public void setStartOn(LocalDateTime startOn) {
        this.startOn = startOn;
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
        return new UserProtocol().new Builder();
    }

    public class Builder {
        public Builder protocolId(Long protocolId) {
            UserProtocol.this.protocolId = protocolId;
            return this;
        }

        public Builder accountId(Long accountId) {
            UserProtocol.this.accountId = accountId;
            return this;
        }

        public Builder name(String name) {
            UserProtocol.this.name = name;
            return this;
        }

        public Builder type(Integer type) {
            UserProtocol.this.type = type;
            return this;
        }

        public Builder minAmount(Long minAmount) {
            UserProtocol.this.minAmount = minAmount;
            return this;
        }

        public Builder maxAmount(Long maxAmount) {
            UserProtocol.this.maxAmount = maxAmount;
            return this;
        }

        public Builder startOn(LocalDateTime startOn) {
            UserProtocol.this.startOn = startOn;
            return this;
        }

        public Builder state(Integer state) {
            UserProtocol.this.state = state;
            return this;
        }

        public Builder description(String description) {
            UserProtocol.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            UserProtocol.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            UserProtocol.this.createdTime = createdTime;
            return this;
        }

        public UserProtocol build() {
            return UserProtocol.this;
        }
    }
}
