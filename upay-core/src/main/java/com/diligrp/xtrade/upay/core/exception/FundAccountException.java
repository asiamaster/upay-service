package com.diligrp.xtrade.upay.core.exception;

/**
 * 资金账号异常类
 */
public class FundAccountException extends PaymentServiceException {
    public FundAccountException(String message) {
        super(message);
    }

    public FundAccountException(int code, String message) {
        super(code, message);
    }

    public FundAccountException(String message, Throwable ex) {
        super(message, ex);
    }
}
