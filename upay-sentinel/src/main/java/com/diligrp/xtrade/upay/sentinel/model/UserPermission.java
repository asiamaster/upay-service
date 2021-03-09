package com.diligrp.xtrade.upay.sentinel.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 账户风控配置模型
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public class UserPermission extends BaseDo {
    // 资金账号ID
    private Long accountId;
    // 充值风控配置
    private String deposit;
    // 提现风控配置
    private String withdraw;
    // 交易风控配置
    private String trade;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(String withdraw) {
        this.withdraw = withdraw;
    }

    public String getTrade() {
        return trade;
    }

    public void setTrade(String trade) {
        this.trade = trade;
    }

    public static Builder builder() {
        return new UserPermission().new Builder();
    }

    public class Builder {

        public Builder accountId(Long accountId) {
            UserPermission.this.accountId = accountId;
            return this;
        }

        public Builder deposit(String deposit) {
            UserPermission.this.deposit = deposit;
            return this;
        }

        public Builder withdraw(String withdraw) {
            UserPermission.this.withdraw = withdraw;
            return this;
        }

        public Builder trade(String trade) {
            UserPermission.this.trade = trade;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            UserPermission.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            UserPermission.this.modifiedTime = modifiedTime;
            return this;
        }

        public UserPermission build() {
            return UserPermission.this;
        }
    }
}
