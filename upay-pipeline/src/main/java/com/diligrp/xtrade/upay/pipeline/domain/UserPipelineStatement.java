package com.diligrp.xtrade.upay.pipeline.domain;

import java.time.LocalDateTime;

/**
 * 客户通道支付记录数据模型
 *
 * @author: brenthuang
 * @date: 2020/12/22
 */
public class UserPipelineStatement {
    // 支付通道
    private String pipeline;
    // 账户
    private String toAccount;
    // 账户名
    private String toName;
    // 渠道ID
    private Integer channelId;
    // 渠道名称
    private String channel;
    // 申请金额-分
    private Long amount;
    // 费用金额-分
    private Long fee;
    // 业务流水号
    private String serialNo;
    // 申请状态
    private Integer state;
    // 申请时间
    private LocalDateTime createdTime;
    // 完成时间
    private LocalDateTime modifiedTime;

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
