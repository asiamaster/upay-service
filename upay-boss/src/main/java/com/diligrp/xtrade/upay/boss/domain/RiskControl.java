package com.diligrp.xtrade.upay.boss.domain;

import com.diligrp.xtrade.upay.sentinel.domain.DepositSentinel;
import com.diligrp.xtrade.upay.sentinel.domain.TradeSentinel;
import com.diligrp.xtrade.upay.sentinel.domain.WithdrawSentinel;

import java.util.List;

/**
 * 交易权限及风控信息
 *
 * @author: brenthuang
 * @date: 2021/03/03
 */
public class RiskControl {
    // 商户ID
    private Long mchId;
    // 资金账号
    private Long accountId;
    // 已有交易权限码
    private List<Integer> permission;
    // 全量权限码
    private List<Option> allPermission;
    // 充值风控
    private DepositSentinel deposit;
    // 提现风控
    private WithdrawSentinel withdraw;
    // 交易风控
    private TradeSentinel trade;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public List<Integer> getPermission() {
        return permission;
    }

    public void setPermission(List<Integer> permission) {
        this.permission = permission;
    }

    public List<Option> getAllPermission() {
        return allPermission;
    }

    public void setAllPermission(List<Option> allPermission) {
        this.allPermission = allPermission;
    }

    public DepositSentinel getDeposit() {
        return deposit;
    }

    public void setDeposit(DepositSentinel deposit) {
        this.deposit = deposit;
    }

    public WithdrawSentinel getWithdraw() {
        return withdraw;
    }

    public void setWithdraw(WithdrawSentinel withdraw) {
        this.withdraw = withdraw;
    }

    public TradeSentinel getTrade() {
        return trade;
    }

    public void setTrade(TradeSentinel trade) {
        this.trade = trade;
    }
}
