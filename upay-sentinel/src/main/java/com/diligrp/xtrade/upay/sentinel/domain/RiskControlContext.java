package com.diligrp.xtrade.upay.sentinel.domain;

import com.diligrp.xtrade.upay.sentinel.service.ISentinelAssistant;

/**
 * 风险控制上下文
 *
 * @author: brenthuang
 * @date: 2021/03/03
 */
public class RiskControlContext {
    // 充值风控
    private DepositSentinel deposit;
    // 提现风控
    private WithdrawSentinel withdraw;
    // 交易风控
    private TradeSentinel trade;
    // 风控执行助手
    private ISentinelAssistant executeAssistant;

    public DepositSentinel forDeposit() {
        return this.deposit;
    }

    /**
     * 用户风控配置覆盖全局风控配置
     */
    public void forDeposit(DepositSentinel deposit) {
        this.deposit = deposit;
        this.deposit.setSentinelAssistant(executeAssistant);
    }

    public WithdrawSentinel forWithdraw() {
        return this.withdraw;
    }

    /**
     * 用户风控配置覆盖全局风控配置
     */
    public void forWithdraw(WithdrawSentinel withdraw) {
        this.withdraw = withdraw;
        this.withdraw.setSentinelAssistant(executeAssistant);
    }

    public TradeSentinel forTrade() {
        return this.trade;
    }

    /**
     * 用户风控配置覆盖全局风控配置
     */
    public void forTrade(TradeSentinel trade) {
        this.trade = trade;
        this.trade.setSentinelAssistant(executeAssistant);

    }

    public void configContext(ISentinelAssistant executeAssistant) {
        this.executeAssistant = executeAssistant;
    }
}
