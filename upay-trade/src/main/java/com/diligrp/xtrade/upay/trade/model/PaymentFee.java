package com.diligrp.xtrade.upay.trade.model;

import com.diligrp.xtrade.shared.domain.BaseDo;
import com.diligrp.xtrade.upay.core.util.Constants;

import java.time.LocalDateTime;

/**
 * 支付费用数据模型
 */
public class PaymentFee extends BaseDo {
    // 支付ID
    private String paymentId;
    // 费用用途
    private Integer useFor;
    // 金额-分
    private Long amount;
    // 费用类型
    private Integer type;
    // 费用描述
    private String typeName;

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getUseFor() {
        return useFor;
    }

    public void setUseFor(Integer useFor) {
        this.useFor = useFor;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    /**
     * 费用是否应用于买家, 规则同PaymentFee.forBuyer
     */
    public boolean forBuyer() {
        return useFor == null ? false : useFor == Constants.FOR_BUYER;
    }

    /**
     * 费用是否应用于卖家-默认为卖家费用, 规则同PaymentFee.forSeller
     */
    public boolean forSeller() {
        return useFor == null ? true : useFor == Constants.FOR_SELLER;
    }

    public static PaymentFee of(String paymentId, Long amount, Integer type, String typeName, LocalDateTime when) {
        return of(paymentId, null, amount, type, typeName, when);
    }

    public static PaymentFee of(String paymentId, Integer useFor, Long amount, Integer type, String typeName, LocalDateTime when) {
        PaymentFee fee = new PaymentFee();
        fee.setPaymentId(paymentId);
        fee.setUseFor(useFor);
        fee.setAmount(amount);
        fee.setType(type);
        fee.setTypeName(typeName);
        fee.setCreatedTime(when);
        return fee;
    }
}
