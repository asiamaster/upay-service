package com.diligrp.xtrade.upay.boss.domain;

import java.time.LocalDate;

/**
 * 客户账单查询
 *
 * @author: brenthuang
 * @date: 2020/10/15
 */
public class ListUserStatement {
    // 页号
    private Integer pageNo = 1;
    // 每页记录数
    private Integer pageSize = 30;
    // 账单类型
    private Integer type;
    // 资金账号
    private Long accountId;
    // 开始时间
    private LocalDate startDate;
    // 结束时间
    private LocalDate endDate;

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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
