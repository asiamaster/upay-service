package com.diligrp.xtrade.upay.sentinel.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 商户风控配置模型
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public class GlobalPermission extends BaseDo {
    // 商户ID
    private Long mchId;
    // 充值风控配置
    private String deposit;
    // 提现风控配置
    private String withdraw;
    // 交易风控配置
    private String trade;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
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
        return new GlobalPermission().new Builder();
    }

    public class Builder {

        public Builder mchId(Long mchId) {
            GlobalPermission.this.mchId = mchId;
            return this;
        }

        public Builder deposit(String deposit) {
            GlobalPermission.this.deposit = deposit;
            return this;
        }

        public Builder withdraw(String withdraw) {
            GlobalPermission.this.withdraw = withdraw;
            return this;
        }

        public Builder trade(String trade) {
            GlobalPermission.this.trade = trade;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            GlobalPermission.this.createdTime = createdTime;
            return this;
        }

        public Builder modifiedTime(LocalDateTime modifiedTime) {
            GlobalPermission.this.modifiedTime = modifiedTime;
            return this;
        }

        public GlobalPermission build() {
            return GlobalPermission.this;
        }
    }
}
