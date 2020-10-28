package com.diligrp.xtrade.upay.channel.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务流水账单数据模型
 */
public class UserStatement extends BaseDo {
    // 交易ID
    private String tradeId;
    // 支付ID
    private String paymentId;
    // 支付渠道
    private Integer channelId;
    // 账号ID
    private Long accountId;
    // 子账号ID
    private Long childId;
    // 流水类型
    private Integer type;
    // 流水说明
    private String typeName;
    // 交易金额-分
    private Long amount;
    // 费用-分
    private Long fee;
    // 期末余额
    private Long balance;
    // 期末冻结金额
    private Long frozenAmount;
    // 业务单号
    private String serialNo;
    // 状态
    private Integer state;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
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

    public Long getChildId() {
        return childId;
    }

    public void setChildId(Long childId) {
        this.childId = childId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
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

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(Long frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public static Builder builder() {
        return new UserStatement().new Builder();
    }

    public class Builder {
        public Builder tradeId(String tradeId) {
            UserStatement.this.tradeId = tradeId;
            return this;
        }

        public Builder paymentId(String paymentId) {
            UserStatement.this.paymentId = paymentId;
            return this;
        }

        public Builder channelId(Integer channelId) {
            UserStatement.this.channelId = channelId;
            return this;
        }

        public Builder accountId(Long accountId, Long parentId) {
            // accountId始终存放主资金账号
            if (parentId == 0) {
                UserStatement.this.accountId = accountId;
            } else {
                UserStatement.this.accountId = parentId;
                UserStatement.this.childId = accountId;
            }
            return this;
        }

        public Builder type(Integer type) {
            UserStatement.this.type = type;
            return this;
        }

        public Builder typeName(String typeName) {
            UserStatement.this.typeName = typeName;
            return this;
        }

        public Builder amount(Long amount) {
            UserStatement.this.amount = amount;
            return this;
        }

        public Builder fee(Long fee) {
            UserStatement.this.fee = fee;
            return this;
        }

        public Builder balance(Long balance) {
            UserStatement.this.balance = balance;
            return this;
        }

        public Builder frozenAmount(Long frozenAmount) {
            UserStatement.this.frozenAmount = frozenAmount;
            return this;
        }

        public Builder serialNo(String serialNo) {
            UserStatement.this.serialNo = serialNo;
            return this;
        }

        public Builder state(Integer state) {
            UserStatement.this.state = state;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            UserStatement.this.createdTime = createdTime;
            return this;
        }

        public UserStatement build() {
            return UserStatement.this;
        }

        public void collect(List<UserStatement> statements) {
            statements.add(UserStatement.this);
        }
    }
}
