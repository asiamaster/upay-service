package com.diligrp.xtrade.upay.trade.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 交易撤销、交易退款请求模型
 */
public class Refund extends HashMap<String, Object> {
    // 原交易号
    private String tradeId;
    // 操作金额
    private Long amount;

    public static Refund of(String tradeId, Long amount) {
        Refund refund = new Refund();
        refund.setTradeId(tradeId);
        refund.setAmount(amount);
        return refund;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
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
