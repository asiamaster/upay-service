package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;

/**
 * 通道响应领域模型
 *
 * @author: brenthuang
 * @date: 2020/12/09
 */
public class PipelineResponse {
    // 通道处理状态
    private ProcessState state;
    // 本地支付流水号
    private String paymentId;
    // 远程通道业务流水号
    private String serialNo;
    // 通道费用
    private Long fee;
    // 备注
    private String description;
    // 本地资金事务状态
    private TransactionStatus status;

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public long fee() {
        return fee != null ? Math.abs(fee) : 0;
    }

    public static PipelineResponse of(ProcessState state, String paymentId, Long fee, TransactionStatus status) {
        PipelineResponse response = new PipelineResponse();
        response.setState(state);
        response.setPaymentId(paymentId);
        response.setFee(fee);
        response.setStatus(status);
        return response;
    }
}
