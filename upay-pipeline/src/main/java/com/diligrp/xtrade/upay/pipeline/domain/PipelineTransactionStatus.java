package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.upay.core.domain.TransactionStatus;

/**
 * 通道事务状态流水
 *
 * @author: brenthuang
 * @date: 2021/01/15
 */
public class PipelineTransactionStatus extends TransactionStatus {
    // 支付通道处理状态
    private Integer state;
    // 支付通道返回信息
    private String message;

    public PipelineTransactionStatus(Integer state, String message, TransactionStatus status) {
        this.state = state;
        this.message = message;
        super.setAccountId(status.getAccountId());
        super.setBalance(status.getBalance());
        super.setAmount(status.getAmount());
        super.setFrozenBalance(status.getFrozenBalance());
        super.setFrozenAmount(status.getFrozenAmount());
        super.setWhen(status.getWhen());
        super.setStreams(status.getStreams());
        super.setRelation(status.getRelation());
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
