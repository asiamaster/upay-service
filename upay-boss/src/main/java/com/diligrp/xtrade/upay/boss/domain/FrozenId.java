package com.diligrp.xtrade.upay.boss.domain;

/**
 * 账号冻结接口层模型
 */
public class FrozenId {
    // 冻结ID
    private Long frozenId;

    public Long getFrozenId() {
        return frozenId;
    }

    public void setFrozenId(Long frozenId) {
        this.frozenId = frozenId;
    }

    public static FrozenId of(Long frozenId) {
        FrozenId frozen = new FrozenId();
        frozen.setFrozenId(frozenId);
        return frozen;
    }
}
