package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.shared.domain.PageQuery;

import java.time.LocalDateTime;

/**
 * 冻结订单查询
 *
 * @author: brenthuang
 * @date: 2020/07/24
 */
public class FrozenOrderQuery extends PageQuery {
    // 资金账号
    private Long accountId;
    // 冻结类型-系统冻结 交易冻结
    private Integer type;
    // 冻结状态
    private Integer state;
    // 开始时间
    private LocalDateTime startTime;
    // 结束时间
    private LocalDateTime endTime;

    public static FrozenOrderQuery of(Long accountId, Integer type, Integer state, LocalDateTime startTime, LocalDateTime endTime) {
        FrozenOrderQuery query = new FrozenOrderQuery();
        query.setAccountId(accountId);
        query.setType(type);
        query.setState(state);
        query.setStartTime(startTime);
        query.setEndTime(endTime);
        return query;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
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
