package com.diligrp.xtrade.upay.pipeline.domain;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * 通道请求领域模型
 *
 * @author: brenthuang
 * @date: 2020/12/09
 */
public class PipelineRequest extends HashMap<String, Object> {
    // 支付通道
    private IPipeline pipeline;
    // 支付流水号
    private String paymentId;
    // 转入账户
    private String toAccount;
    // 转入账户名
    private String toName;
    // 转入账户类型
    private Integer toType;
    // 交易金额
    private Long amount;
    // 收款银行-联行行号
    private String bankNo;
    // 收款银行-名称
    private String bankName;
    // 交易时间
    private LocalDateTime when;

    public static PipelineRequest of(IPipeline pipeline, String paymentId, String toAccount, String toName,
                                     Integer toType, Long amount, String bankNo, String bankName, LocalDateTime when) {
        PipelineRequest request = new PipelineRequest();
        request.pipeline = pipeline;
        request.paymentId = paymentId;
        request.toAccount = toAccount;
        request.toName = toName;
        request.toType = toType;
        request.amount = amount;
        request.bankNo = bankNo;
        request.bankName = bankName;
        request.when = when;
        return request;
    }

    public IPipeline getPipeline() {
        return pipeline;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getToAccount() {
        return toAccount;
    }

    public String getToName() {
        return toName;
    }

    public Integer getToType() {
        return toType;
    }

    public Long getAmount() {
        return amount;
    }

    public String getBankNo() {
        return bankNo;
    }

    public String getBankName() {
        return bankName;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public PipelineRequest attach(Object object) {
        put(object.getClass().getName(), object);
        return this;
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

    public <T> T getObject(Class<T> type) {
        Object value = get(type.getName());
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
