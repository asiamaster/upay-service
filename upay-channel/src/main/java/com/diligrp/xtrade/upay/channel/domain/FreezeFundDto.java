package com.diligrp.xtrade.upay.channel.domain;

/**
 * 资金冻结数据传输模型
 */
public class FreezeFundDto {
    // 资金账号ID
    private Long accountId;
    // 业务账号
    private Long businessId;
    // 类型 - 系统冻结和交易冻结
    private Integer type;
    // 冻结金额 - 分
    private Long amount;
    // 描述
    private String description;

    public static FreezeFundDto of(Long accountId, Long businessId, Integer type, Long amount) {
        FreezeFundDto frozenOrder = new FreezeFundDto();
        frozenOrder.setAccountId(accountId);
        frozenOrder.setBusinessId(businessId);
        frozenOrder.setType(type);
        frozenOrder.setAmount(amount);
        return frozenOrder;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
