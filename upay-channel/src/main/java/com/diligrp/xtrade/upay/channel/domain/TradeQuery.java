package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.shared.domain.PageQuery;

import java.time.LocalDate;

/**
 * 客户交易流水查询
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public class TradeQuery extends PageQuery {
    // 资金账号
    private Long accountId;
    // 开始日期
    private LocalDate startDate;
    // 结束日期
    private LocalDate endDate;

    public static TradeQuery of(Long accountId, LocalDate startDate, LocalDate endDate) {
        TradeQuery query = new TradeQuery();
        query.setAccountId(accountId);
        query.setStartDate(startDate);
        query.setEndDate(endDate);
        return query;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
