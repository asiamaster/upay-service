package com.diligrp.xtrade.upay.core.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 账户资金数据模型
 */
public class AccountFund extends BaseDo {
    // 账号ID
    private Long accountId;
    // 账户余额-分
    private Long balance;
    // 冻结金额-分
    private Long frozenAmount;
    // 担保金额-分
    private Long vouchAmount;
    // 日切金额-分
    private Long dailyAmount;
    // 数据版本号
    private Integer version;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public Long getVouchAmount() {
        return vouchAmount;
    }

    public void setVouchAmount(Long vouchAmount) {
        this.vouchAmount = vouchAmount;
    }

    public Long getDailyAmount() {
        return dailyAmount;
    }

    public void setDailyAmount(Long dailyAmount) {
        this.dailyAmount = dailyAmount;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public static Builder builder() {
        return new AccountFund().new Builder();
    }

    public class Builder {
        public Builder accountId(Long accountId) {
            AccountFund.this.accountId = accountId;
            return this;
        }

        public Builder balance(Long balance) {
            AccountFund.this.balance = balance;
            return this;
        }

        public Builder frozenAmount(Long frozenAmount) {
            AccountFund.this.frozenAmount = frozenAmount;
            return this;
        }

        public Builder vouchAmount(Long vouchAmount) {
            AccountFund.this.vouchAmount = vouchAmount;
            return this;
        }

        public Builder dailyAmount(Long dailyAmount) {
            AccountFund.this.dailyAmount = dailyAmount;
            return this;
        }

        public Builder version(Integer version) {
            AccountFund.this.version = version;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            AccountFund.this.createdTime = createdTime;
            return this;
        }

        public AccountFund build() {
            return AccountFund.this;
        }
    }
}
