package com.diligrp.xtrade.upay.boss.domain;

/**
 * 免密支付协议模型
 */
public class ProtocolId {
    private Long protocolId;

    public Long getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(Long protocolId) {
        this.protocolId = protocolId;
    }

    public static ProtocolId of(Long protocolId) {
        ProtocolId protocol = new ProtocolId();
        protocol.setProtocolId(protocolId);
        return protocol;
    }
}
