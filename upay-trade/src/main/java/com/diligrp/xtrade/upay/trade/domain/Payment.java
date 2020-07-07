package com.diligrp.xtrade.upay.trade.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 交易支付模型
 */
public class Payment extends HashMap<String, Object> {
    // 资金账号ID
    private Long accountId;
    // 业务账号ID
    private Long businessId;
    // 支付金额
    private Long amount;
    // 支付渠道
    private Integer channelId;
    // 支付密码
    private String password;

    public static Payment of(Long accountId, Long businessId, Long amount, Integer channelId, String password) {
        Payment payment = new Payment();
        payment.setAccountId(accountId);
        payment.setBusinessId(businessId);
        payment.setAmount(amount);
        payment.setChannelId(channelId);
        payment.setPassword(password);
        return payment;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
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
