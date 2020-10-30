package com.diligrp.xtrade.upay.channel.domain;

/**
 * 客户账单过滤器
 *
 * @author: brenthuang
 * @date: 2020/10/28
 */
public class UserStatementFilter {
    // 交易号
    private String tradeId;
    // 资金账号ID
    private Long accountId;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public static UserStatementFilter of(String tradeId, Long accountId) {
        UserStatementFilter filter = new UserStatementFilter();
        filter.setTradeId(tradeId);
        filter.setAccountId(accountId);
        return filter;
    }
}
