package com.diligrp.xtrade.upay.trade.domain;

import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.core.util.Constants;

/**
 * 交易费用模型
 */
public class Fee {
    // 金额-元
    private Long amount;
    // 费用类型
    private Integer type;
    // 费用描述
    private String typeName;
    // 费用用途 - 买家/卖家
    private Integer useFor;

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

    public Integer getUseFor() {
        return useFor;
    }

    public void setUseFor(Integer useFor) {
        this.useFor = useFor;
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

    public void checkUseFor() {
        AssertUtils.isTrue(useFor == null || useFor == Constants.FOR_BUYER ||
            useFor == Constants.FOR_SELLER, "invalid fee useFor");
    }
}