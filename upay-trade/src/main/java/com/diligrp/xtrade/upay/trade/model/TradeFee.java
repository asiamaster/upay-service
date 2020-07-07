package com.diligrp.xtrade.upay.trade.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 交易费用数据模型
 */
public class TradeFee extends BaseDo {
    // 交易ID
    private String tradeId;
    // 金额-分
    private Long amount;
    // 费用类型
    private Integer type;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
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

    public static TradeFee of(String tradeId, Long amount, Integer type, LocalDateTime when) {
        TradeFee fee = new TradeFee();
        fee.setTradeId(tradeId);
        fee.setAmount(amount);
        fee.setType(type);
        fee.setCreatedTime(when);
        return fee;
    }
}
