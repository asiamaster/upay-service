package com.diligrp.xtrade.upay.pipeline.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 通道状态列表
 */
public enum PipelineState implements IEnumType {

    NORMAL("启用", 1),

    VOID("禁用", 2);

    private String name;
    private int code;

    PipelineState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<PipelineState> getState(int code) {
        Stream<PipelineState> STATES = Arrays.stream(PipelineState.values());
        return STATES.filter(state -> state.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<PipelineState> STATES = Arrays.stream(PipelineState.values());
        Optional<String> result = STATES.filter(state -> state.getCode() == code)
            .map(PipelineState::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<PipelineState> getStateList() {
        return Arrays.asList(PipelineState.values());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }
}
