package com.diligrp.xtrade.upay.core.exception;

import com.diligrp.xtrade.upay.core.ErrorCode;

/**
 * 支付服务异常类
 */
public class PaymentServiceException extends RuntimeException {
    /**
     * 错误码
     */
    private int code = ErrorCode.SYSTEM_UNKNOWN_ERROR;

    /**
     * 是否打印异常栈
     */
    private boolean stackTrace = true;

    public PaymentServiceException(String message) {
        super(message);
    }

    public PaymentServiceException(int code, String message) {
        super(message);
        this.code = code;
        this.stackTrace = false;
    }

    public PaymentServiceException(String message, Throwable ex) {
        super(message, ex);
    }

    @Override
    public Throwable fillInStackTrace() {
        return stackTrace ? super.fillInStackTrace() : this;
    }

    public int getCode() {
        return code;
    }
}
