package com.diligrp.xtrade.upay.trade.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 交易支付数据模型
 */
public class TradePayment extends BaseDo {
    // 支付ID
    private String paymentId;
    // 交易ID
    private String tradeId;
    // 支付渠道
    private Integer channelId;
    // 账号ID
    private Long accountId;
    // 业务账号
    private Long businessId;
    // 账号名称
    private String name;
    // 银行卡号
    private String cardNo;
    // 金额-分
    private Long amount;
    // 费用金额-分
    private Long fee;
    // 支付状态
    private Integer state;
    // 备注
    private String description;
    // 数据版本号
    private Integer version;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
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

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
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
        return new TradePayment().new Builder();
    }

    public class Builder {
        public Builder paymentId(String paymentId) {
            TradePayment.this.paymentId = paymentId;
            return this;
        }

        public Builder tradeId(String tradeId) {
            TradePayment.this.tradeId = tradeId;
            return this;
        }

        public Builder channelId(Integer channelId) {
            TradePayment.this.channelId = channelId;
            return this;
        }

        public Builder accountId(Long accountId) {
            TradePayment.this.accountId = accountId;
            return this;
        }

        public Builder businessId(Long businessId) {
            TradePayment.this.businessId = businessId;
            return this;
        }

        public Builder name(String name) {
            TradePayment.this.name = name;
            return this;
        }

        public Builder cardNo(String cardNo) {
            TradePayment.this.cardNo = cardNo;
            return this;
        }

        public Builder amount(Long amount) {
            TradePayment.this.amount = amount;
            return this;
        }

        public Builder fee(Long fee) {
            TradePayment.this.fee = fee;
            return this;
        }

        public Builder state(Integer state) {
            TradePayment.this.state = state;
            return this;
        }

        public Builder description(String description) {
            TradePayment.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            TradePayment.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            TradePayment.this.createdTime = createdTime;
            return this;
        }

        public TradePayment build() {
            return TradePayment.this;
        }
    }
}
