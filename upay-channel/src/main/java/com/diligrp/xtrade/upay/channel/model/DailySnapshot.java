package com.diligrp.xtrade.upay.channel.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账户余额快照数据模型
 *
 * @author: brenthuang
 * @date: 2020/07/29
 */
public class DailySnapshot extends BaseDo {
    // 资金账号
    private Long accountId;
    // 账户余额 - 分
    private Long balance;
    // 冻结金额 - 分
    private Long frozenAmount;
    // 快照日期
    private LocalDate snapshotOn;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBalance() {
        return balance;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public Long getFrozenAmount() {
        return frozenAmount;
    }

    public void setFrozenAmount(Long frozenAmount) {
        this.frozenAmount = frozenAmount;
    }

    public LocalDate getSnapshotOn() {
        return snapshotOn;
    }

    public void setSnapshotOn(LocalDate snapshotOn) {
        this.snapshotOn = snapshotOn;
    }

    public static DailySnapshot of(Long accountId, Long balance, Long frozenAmount, LocalDate snapshotOn, LocalDateTime when) {
        DailySnapshot snapshot = new DailySnapshot();
        snapshot.setAccountId(accountId);
        snapshot.setBalance(balance);
        snapshot.setFrozenAmount(frozenAmount);
        snapshot.setSnapshotOn(snapshotOn);
        snapshot.setCreatedTime(when);
        return snapshot;
    }
}
