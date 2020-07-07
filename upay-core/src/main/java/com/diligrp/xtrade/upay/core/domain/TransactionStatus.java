package com.diligrp.xtrade.upay.core.domain;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事务状态
 */
public class TransactionStatus {
    // 账户ID
    private Long accountId;
    // 期初余额-分
    private Long balance;
    // 操作金额-分(正值 负值)
    private Long amount;
    // 发生时间
    private LocalDateTime when;
    // 收支明细
    private List<FundStream> streams;

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

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public void setWhen(LocalDateTime when) {
        this.when = when;
    }

    public List<FundStream> getStreams() {
        return streams;
    }

    public void setStreams(List<FundStream> streams) {
        this.streams = streams;
    }

    public static TransactionStatus of(Long accountId, Long balance, Long amount, LocalDateTime when) {
        TransactionStatus status = new TransactionStatus();
        status.setAccountId(accountId);
        status.setBalance(balance);
        status.setAmount(amount);
        status.setWhen(when);
        return status;
    }

    public void ofStreams(List<FundStream> streams) {
        this.streams = streams;
    }

    public static class FundStream {
        // 期初余额-分
        private Long balance;
        // 操作金额-分(正值 负值)
        private Long amount;
        // 资金类型
        private Integer type;
        // 类型名称
        private String typeName;

        public Long getBalance() {
            return balance;
        }

        public void setBalance(Long balance) {
            this.balance = balance;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public String getTypeName() {
            return typeName;
        }

        public void setTypeName(String typeName) {
            this.typeName = typeName;
        }

        public static FundStream of(Long balance, Long amount, Integer type, String typeName) {
            FundStream stream = new FundStream();
            stream.setBalance(balance);
            stream.setAmount(amount);
            stream.setType(type);
            stream.setTypeName(typeName);
            return stream;
        }
    }

}
