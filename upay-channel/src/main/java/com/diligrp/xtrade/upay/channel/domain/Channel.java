package com.diligrp.xtrade.upay.channel.domain;

/**
 * 支付渠道模型
 *
 * @author: brenthuang
 * @date: 2021/01/06
 */
public class Channel {
    // 渠道ID
    private Integer channelId;
    // 渠道名称
    private String channelName;

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public static Channel of(Integer channelId, String channelName) {
        Channel channel = new Channel();
        channel.setChannelId(channelId);
        channel.setChannelName(channelName);
        return channel;
    }
}
