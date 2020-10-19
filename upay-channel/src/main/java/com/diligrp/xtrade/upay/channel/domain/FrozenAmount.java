package com.diligrp.xtrade.upay.channel.domain;

/**
 * 冻结金额明细
 *
 * @author: brenthuang
 * @date: 2020/10/16
 */
public class FrozenAmount {
    // 资金账号ID
    private Long accountId;
    // 交易冻结金额
    private Long tradeFrozen;
    // 系统冻结金额
    private Long manFrozen;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getTradeFrozen() {
        return tradeFrozen;
    }

    public void setTradeFrozen(Long tradeFrozen) {
        this.tradeFrozen = tradeFrozen;
    }

    public Long getManFrozen() {
        return manFrozen;
    }

    public void setManFrozen(Long manFrozen) {
        this.manFrozen = manFrozen;
    }

    public static FrozenAmount of(Long accountId, Long tradeFrozen, Long manFrozen) {
        FrozenAmount frozenAmount = new FrozenAmount();
        frozenAmount.setAccountId(accountId);
        frozenAmount.setTradeFrozen(tradeFrozen);
        frozenAmount.setManFrozen(manFrozen);
        return frozenAmount;
    }
}
