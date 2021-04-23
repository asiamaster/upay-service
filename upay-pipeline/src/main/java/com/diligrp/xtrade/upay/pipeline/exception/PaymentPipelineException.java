package com.diligrp.xtrade.upay.pipeline.exception;

import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;

/**
 * 支付通道异常类
 */
public class PaymentPipelineException extends PaymentServiceException {
    public PaymentPipelineException(String message) {
        super(message);
    }

    public PaymentPipelineException(int code, String message) {
        super(code, message);
    }

    public PaymentPipelineException(String message, Throwable ex) {
        super(message, ex);
    }
}
