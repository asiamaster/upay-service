package com.diligrp.xtrade.upay.sentinel.domain;

import com.diligrp.xtrade.upay.sentinel.service.ISentinelAssistant;

/**
 * 风控哨兵模型
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public abstract class Sentinel {
    protected ISentinelAssistant sentinelAssistant;

    abstract void checkPassport(Passport passport);

    abstract void admitPassport(Passport passport);

    public void setSentinelAssistant(ISentinelAssistant sentinelAssistant) {
        this.sentinelAssistant = sentinelAssistant;
    }
}
