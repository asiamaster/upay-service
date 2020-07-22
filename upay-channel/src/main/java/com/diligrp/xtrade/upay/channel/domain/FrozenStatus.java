package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.upay.core.domain.TransactionStatus;

/**
 * 冻结/解冻资金状态领域模型
 */
public class FrozenStatus {
    // 冻结ID
    private Long frozenId;
    // 账户资金事务状态
    private TransactionStatus transaction;

    public Long getFrozenId() {
        return frozenId;
    }

    public void setFrozenId(Long frozenId) {
        this.frozenId = frozenId;
    }

    public TransactionStatus getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionStatus transaction) {
        this.transaction = transaction;
    }

    public static FrozenStatus of(Long frozenId, TransactionStatus status) {
        FrozenStatus frozen = new FrozenStatus();
        frozen.setFrozenId(frozenId);
        frozen.setTransaction(status);
        return frozen;
    }
}
