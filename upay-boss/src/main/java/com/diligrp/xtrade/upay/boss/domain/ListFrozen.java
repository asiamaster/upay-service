package com.diligrp.xtrade.upay.boss.domain;

import java.time.LocalDateTime;

/**
 * 查询人工冻结资金记录
 *
 * @author: brenthuang
 * @date: 2020/07/24
 */
public class ListFrozen {
    // 页号
    private Integer pageNum = 1;
    // 每页记录数
    private Integer pageSize = 20;
    // 资金账号
    private Long accountId;
    // 冻结状态
    private Integer state;
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
