package com.diligrp.xtrade.upay.channel.domain;

import java.time.LocalDateTime;

/**
 * 渠道流水数据模型
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public class TradeStatement {
    // 交易ID
    private String tradeId;
    // 交易类型
    private Integer type;
    // 业务单号
    private String serialNo;
    // A端资金账号ID
    private Long accountId1;
    // A端费用
    private Long fee1;
    // 交易状态
    private Integer state;
    // 资金渠道
    private Integer channelId;
    // B端资金账号ID
    private Long accountId2;
    // B端费用
    private Long fee2;
    // 交易金额
    private Long amount;
    // 交易时间
    private LocalDateTime payTime;
    // 退款金额
    private Long refundAmount;
    // 退款时间
    private LocalDateTime refundTime;
    // 交易备注
    private String description;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Long getAccountId1() {
        return accountId1;
    }

    public void setAccountId1(Long accountId1) {
        this.accountId1 = accountId1;
    }

    public Long getFee1() {
        return fee1;
    }

    public void setFee1(Long fee1) {
        this.fee1 = fee1;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public Long getAccountId2() {
        return accountId2;
    }

    public void setAccountId2(Long accountId2) {
        this.accountId2 = accountId2;
    }

    public Long getFee2() {
        return fee2;
    }

    public void setFee2(Long fee2) {
        this.fee2 = fee2;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public LocalDateTime getPayTime() {
        return payTime;
    }

    public void setPayTime(LocalDateTime payTime) {
        this.payTime = payTime;
    }

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
