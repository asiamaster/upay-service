package com.diligrp.xtrade.upay.trade.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 退款支付数据模型
 */
public class RefundPayment extends BaseDo {
    // 退款支付ID
    private String paymentId;
    // 退款类型
    private Integer type;
    // 原交易ID
    private String tradeId;
    // 原交易类型
    private Integer tradeType;
    // 金额-分
    private Long amount;
    // 费用金额-分
    private Long fee;
    // 状态
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public void setTradeType(Integer tradeType) {
        this.tradeType = tradeType;
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
        return new RefundPayment().new Builder();
    }

    public class Builder {
        public Builder paymentId(String paymentId) {
            RefundPayment.this.paymentId = paymentId;
            return this;
        }

        public Builder type(Integer type) {
            RefundPayment.this.type = type;
            return this;
        }

        public Builder tradeId(String tradeId) {
            RefundPayment.this.tradeId = tradeId;
            return this;
        }

        public Builder tradeType(Integer tradeType) {
            RefundPayment.this.tradeType = tradeType;
            return this;
        }

        public Builder amount(Long amount) {
            RefundPayment.this.amount = amount;
            return this;
        }

        public Builder fee(Long fee) {
            RefundPayment.this.fee = fee;
            return this;
        }

        public Builder state(Integer state) {
            RefundPayment.this.state = state;
            return this;
        }

        public Builder description(String description) {
            RefundPayment.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            RefundPayment.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            RefundPayment.this.createdTime = createdTime;
            return this;
        }

        public RefundPayment build() {
            return RefundPayment.this;
        }
    }
}
