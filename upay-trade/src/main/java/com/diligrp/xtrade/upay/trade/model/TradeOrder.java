package com.diligrp.xtrade.upay.trade.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 交易订单数据模型
 */
public class TradeOrder extends BaseDo {
    // 商户ID
    private Long mchId;
    // 应用ID
    private Long appId;
    // 交易ID
    private String tradeId;
    // 交易类型
    private Integer type;
    // 外部流水号
    private String serialNo;
    // 账务周期号
    private String cycleNo;
    // 账号ID
    private Long accountId;
    // 业务账号ID
    private Long businessId;
    // 账号名称
    private String name;
    // 金额-分
    private Long amount;
    // 初始金额-分
    private Long maxAmount;
    // 费用金额-分
    private Long fee;
    // 交易状态
    private Integer state;
    // 备注
    private String description;
    // 数据版本号
    private Integer version;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getCycleNo() {
        return cycleNo;
    }

    public void setCycleNo(String cycleNo) {
        this.cycleNo = cycleNo;
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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
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
        return new TradeOrder().new Builder();
    }

    public class Builder {
        public Builder mchId(Long mchId) {
            TradeOrder.this.mchId = mchId;
            return this;
        }

        public Builder appId(Long appId) {
            TradeOrder.this.appId = appId;
            return this;
        }

        public Builder tradeId(String tradeId) {
            TradeOrder.this.tradeId = tradeId;
            return this;
        }

        public Builder type(Integer type) {
            TradeOrder.this.type = type;
            return this;
        }

        public Builder serialNo(String serialNo) {
            TradeOrder.this.serialNo = serialNo;
            return this;
        }

        public Builder cycleNo(String cycleNo) {
            TradeOrder.this.cycleNo = cycleNo;
            return this;
        }

        public Builder accountId(Long accountId) {
            TradeOrder.this.accountId = accountId;
            return this;
        }

        public Builder businessId(Long businessId) {
            TradeOrder.this.businessId = businessId;
            return this;
        }

        public Builder name(String name) {
            TradeOrder.this.name = name;
            return this;
        }

        public Builder amount(Long amount) {
            TradeOrder.this.amount = amount;
            return this;
        }

        public Builder maxAmount(Long maxAmount) {
            TradeOrder.this.maxAmount = maxAmount;
            return this;
        }

        public Builder fee(Long fee) {
            TradeOrder.this.fee = fee;
            return this;
        }

        public Builder state(Integer state) {
            TradeOrder.this.state = state;
            return this;
        }

        public Builder description(String description) {
            TradeOrder.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            TradeOrder.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            TradeOrder.this.createdTime = createdTime;
            return this;
        }

        public TradeOrder build() {
            return TradeOrder.this;
        }
    }
}
