package com.diligrp.xtrade.upay.boss.domain;

/**
 * 客户ID接口层模型
 */
public class CustomerId {
    private Long customerId;

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public static CustomerId of(Long id) {
        CustomerId customerId = new CustomerId();
        customerId.setCustomerId(id);
        return customerId;
    }
}
