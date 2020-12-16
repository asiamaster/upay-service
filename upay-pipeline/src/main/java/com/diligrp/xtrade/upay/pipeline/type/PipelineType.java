package com.diligrp.xtrade.upay.pipeline.type;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 支持的支付通道
 */
public enum PipelineType {
    SJ_BANK("盛京银行", "SJB");

    private String name;
    private String code;

    PipelineType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(String code) {
        return this.code.equals(code);
    }

    public static Optional<PipelineType> getType(String code) {
        Stream<PipelineType> TYPES = Arrays.stream(PipelineType.values());
        return TYPES.filter(type -> type.getCode().equalsIgnoreCase(code)).findFirst();
    }

    public static String getName(String code) {
        Stream<PipelineType> TYPES = Arrays.stream(PipelineType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode().equalsIgnoreCase(code))
            .map(PipelineType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<PipelineType> getTypeList() {
        return Arrays.asList(PipelineType.values());
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
