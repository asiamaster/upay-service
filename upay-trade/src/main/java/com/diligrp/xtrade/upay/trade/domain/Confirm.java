package com.diligrp.xtrade.upay.trade.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 确认交易模型-适用于预授权交易
 */
public class Confirm extends HashMap<String, Object> {
    // 交易账户ID
    private Long accountId;
    // 操作金额
    private Long amount;
    // 支付密码
    private String password;

    public static Confirm of(Long accountId, Long amount, String password) {
        Confirm confirm = new Confirm();
        confirm.setAccountId(accountId);
        confirm.setAmount(amount);
        confirm.setPassword(password);
        return confirm;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getString(String param) {
        return (String)get(param);
    }

    public Long getLong(String param) {
        Object value = get(param);
        if (value != null) {
            return value instanceof Long ? (Long)value : Long.parseLong(value.toString());
        }
        return null;
    }

    public Integer getInteger(String param) {
        Object value = get(param);
        if (value != null) {
            return value instanceof Integer ? (Integer)value : Integer.parseInt(value.toString());
        }
        return null;
    }

    public <T> T getObject(String param, Class<T> type) {
        Object value = get(param);
        return value == null ? null : type.cast(value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getObject(String param) {
        Object value = get(param);
        return Optional.ofNullable ((T) value);
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<List<T>> getObjects(String param) {
        Object value = get(param);
        return Optional.ofNullable ((List<T>) value);
    }
}
