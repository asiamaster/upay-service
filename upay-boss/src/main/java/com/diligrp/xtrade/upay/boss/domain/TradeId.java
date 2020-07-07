package com.diligrp.xtrade.upay.boss.domain;

/**
 * 交易ID接口层模型
 */
public class TradeId {
    // 交易ID
    private String tradeId;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public static TradeId of(String tradeId) {
        TradeId trade = new TradeId();
        trade.setTradeId(tradeId);
        return trade;
    }
}
