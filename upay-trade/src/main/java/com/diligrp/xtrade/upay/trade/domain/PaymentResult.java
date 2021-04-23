package com.diligrp.xtrade.upay.trade.domain;

import com.diligrp.xtrade.upay.core.domain.TransactionStatus;

import java.util.HashMap;

/**
 * 支付结果模型
 */
public class PaymentResult extends HashMap<String, Object> {
    // 交易成功
    public static final int CODE_SUCCESS = 200;

    // 交易状态
    private int code;
    // 支付ID
    private String paymentId;
    // 账户资金
    private TransactionStatus status;
    // 支付信息描述
    private String message = "success";

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static PaymentResult of(int code, String paymentId, TransactionStatus status) {
        PaymentResult paymentState = new PaymentResult();
        paymentState.setPaymentId(paymentId);
        paymentState.setCode(code);
        paymentState.setStatus(status);
        return paymentState;
    }

    public static PaymentResult of(int code, String paymentId, TransactionStatus status, String message) {
        PaymentResult paymentState = new PaymentResult();
        paymentState.setPaymentId(paymentId);
        paymentState.setCode(code);
        paymentState.setStatus(status);
        paymentState.setMessage(message);
        return paymentState;
    }
}
