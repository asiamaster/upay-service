package com.diligrp.xtrade.upay.pipeline.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 通道支付申请数据模型
 *
 * @author: brenthuang
 * @date: 2020/12/14
 */
public class PipelinePayment extends BaseDo {
    // 支付ID
    private String paymentId;
    // 交易ID
    private String tradeId;
    // 通道编码
    private String code;
    // 通道账户
    private String toAccount;
    // 通道账户名
    private String toName;
    // 账户类型
    private Integer toType;
    // 银行联行行号
    private String bankNo;
    // 银行行名
    private String bankName;
    // 通道流水号
    private String serialNo;
    // 申请金额-分
    private Long amount;
    // 费用金额-分
    private Long fee;
    // 申请状态
    private Integer state;
    // 备注
    private String description;
    // 数据版本号
    private Integer version;
    // 重试次数
    private Integer retryCount;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Integer getToType() {
        return toType;
    }

    public void setToType(Integer toType) {
        this.toType = toType;
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

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
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

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public static Builder builder() {
        return new PipelinePayment().new Builder();
    }

    public class Builder {
        public Builder paymentId(String paymentId) {
            PipelinePayment.this.paymentId = paymentId;
            return this;
        }

        public Builder tradeId(String tradeId) {
            PipelinePayment.this.tradeId = tradeId;
            return this;
        }

        public Builder code(String code) {
            PipelinePayment.this.code = code;
            return this;
        }

        public Builder toAccount(String toAccount) {
            PipelinePayment.this.toAccount = toAccount;
            return this;
        }

        public Builder toName(String toName) {
            PipelinePayment.this.toName = toName;
            return this;
        }

        public Builder toType(Integer toType) {
            PipelinePayment.this.toType = toType;
            return this;
        }

        public Builder bankNo(String bankNo) {
            PipelinePayment.this.bankNo = bankNo;
            return this;
        }

        public Builder bankName(String bankName) {
            PipelinePayment.this.bankName = bankName;
            return this;
        }

        public Builder serialNo(String serialNo) {
            PipelinePayment.this.serialNo = serialNo;
            return this;
        }

        public Builder amount(Long amount) {
            PipelinePayment.this.amount = amount;
            return this;
        }

        public Builder fee(Long fee) {
            PipelinePayment.this.fee = fee;
            return this;
        }

        public Builder state(Integer state) {
            PipelinePayment.this.state = state;
            return this;
        }

        public Builder description(String description) {
            PipelinePayment.this.description = description;
            return this;
        }

        public Builder version(Integer version) {
            PipelinePayment.this.version = version;
            return this;
        }

        public Builder retryCount(Integer retryCount) {
            PipelinePayment.this.retryCount = retryCount;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            PipelinePayment.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            PipelinePayment.this.modifiedTime = modifiedTime;
            return this;
        }

        public PipelinePayment build() {
            return PipelinePayment.this;
        }
    }
}
