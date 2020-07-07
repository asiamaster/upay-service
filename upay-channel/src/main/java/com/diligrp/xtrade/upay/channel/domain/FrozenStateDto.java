package com.diligrp.xtrade.upay.channel.domain;

import java.time.LocalDateTime;

public class FrozenStateDto {
    // 冻结ID
    private Long frozenId;
    // 状态 - 冻结、解冻
    private Integer state;
    // 数据版本
    private Integer version;
    // 修改时间
    private LocalDateTime modifiedTime;

    public static FrozenStateDto of(Long frozenId, Integer state, Integer version, LocalDateTime modifiedTime) {
        FrozenStateDto updateState = new FrozenStateDto();
        updateState.setFrozenId(frozenId);
        updateState.setState(state);
        updateState.setVersion(version);
        updateState.setModifiedTime(modifiedTime);
        return updateState;
    }

    public Long getFrozenId() {
        return frozenId;
    }

    public void setFrozenId(Long frozenId) {
        this.frozenId = frozenId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
