package com.diligrp.xtrade.upay.channel.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账户余额快照监视数据模型
 *
 * @author: brenthuang
 * @date: 2020/07/29
 */
public class SnapshotGuard extends BaseDo {
    // 快照生成中
    public static final int STATE_PENDING = 1;
    // 快照完成
    public static final int STATE_DONE = 2;

    // 快照日期
    private LocalDate snapshotOn;
    // 快照状态
    private Integer state;

    public LocalDate getSnapshotOn() {
        return snapshotOn;
    }

    public void setSnapshotOn(LocalDate snapshotOn) {
        this.snapshotOn = snapshotOn;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void success() {
        setState(STATE_DONE);
        setModifiedTime(LocalDateTime.now());
    }

    public static SnapshotGuard of(LocalDate shapshotOn, Integer state, LocalDateTime when) {
        SnapshotGuard guard = new SnapshotGuard();
        guard.setSnapshotOn(shapshotOn);
        guard.setState(state);
        guard.setCreatedTime(when);
        return guard;
    }
}
