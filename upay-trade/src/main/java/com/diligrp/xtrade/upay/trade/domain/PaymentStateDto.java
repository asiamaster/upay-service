package com.diligrp.xtrade.upay.trade.domain;

import java.time.LocalDateTime;

/**
 * 支付状态数据传输对象
 */
public class PaymentStateDto {
    // 支付ID
    private String paymentId;
    // 金额 - 预支付交易或发生退款时需要更新实际付款金额
    private Long amount;
    // 费用
    private Long fee;
    // 状态
    private Integer state;
    // 数据版本
    private Integer version;
    // 修改时间
    private LocalDateTime modifiedTime;

    public static PaymentStateDto of(String paymentId, Integer state, Integer version, LocalDateTime modifiedTime) {
        return PaymentStateDto.of(paymentId, null, state, version, modifiedTime);
    }

    public static PaymentStateDto of(String paymentId, Long amount, Integer state, Integer version, LocalDateTime modifiedTime) {
        return PaymentStateDto.of(paymentId, amount, null, state, version, modifiedTime);
    }

    public static PaymentStateDto of(String paymentId, Long amount, Long fee, Integer state, Integer version, LocalDateTime modifiedTime) {
        PaymentStateDto paymentState = new PaymentStateDto();
        paymentState.setPaymentId(paymentId);
        paymentState.setAmount(amount);
        paymentState.setFee(fee);
        paymentState.setState(state);
        paymentState.setVersion(version);
        paymentState.setModifiedTime(modifiedTime);
        return paymentState;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
