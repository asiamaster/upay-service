package com.diligrp.xtrade.upay.trade.exception;

import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;

/**
 * 交易支付异常类
 */
public class TradePaymentException extends PaymentServiceException {
    public TradePaymentException(String message) {
        super(message);
    }

    public TradePaymentException(int code, String message) {
        super(code, message);
    }

    public TradePaymentException(String message, Throwable ex) {
        super(message, ex);
    }
}
