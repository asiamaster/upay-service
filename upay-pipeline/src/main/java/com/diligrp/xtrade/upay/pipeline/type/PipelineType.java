package com.diligrp.xtrade.upay.pipeline.type;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 支持的支付通道
 */
public enum PipelineType {
    // 盛京银行银企直连通道
    SJB_DIRECT(28);

    // 所属渠道
    private int channelId;

    PipelineType(int channelId) {
        this.channelId = channelId;
    }

    public boolean equalTo(String code) {
        return this.code().equals(code);
    }

    public static Optional<PipelineType> getType(String code) {
        Stream<PipelineType> TYPES = Arrays.stream(PipelineType.values());
        return TYPES.filter(type -> type.code().equalsIgnoreCase(code)).findFirst();
    }

    public static List<PipelineType> getTypeList() {
        return Arrays.asList(PipelineType.values());
    }

    public String code() {
        return this.name();
    }

    public int getChannelId() {
        return channelId;
    }
}
