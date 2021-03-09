package com.diligrp.xtrade.upay.sentinel.domain;

import com.diligrp.xtrade.upay.sentinel.service.IExecuteAssistant;

/**
 * 风控哨兵模型
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public abstract class Sentinel {
    protected IExecuteAssistant executeAssistant;

    abstract void checkPassport(Passport passport);

    abstract void admitPassport(Passport passport);

    public void setExecuteAssistant(IExecuteAssistant executeAssistant) {
        this.executeAssistant = executeAssistant;
    }
}
