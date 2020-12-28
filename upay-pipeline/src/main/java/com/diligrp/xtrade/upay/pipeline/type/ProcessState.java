package com.diligrp.xtrade.upay.pipeline.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 通道处理状态列表
 */
public enum ProcessState implements IEnumType {
    // 通道处理中
    PROCESSING("处理中", 2),
    // 通道返回支付成功
    SUCCESS("支付成功", 4),
    // 通道返回支付失败
    FAILED("支付失败", 5);

    private String name;
    private int code;

    ProcessState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<ProcessState> getState(int code) {
        Stream<ProcessState> STATES = Arrays.stream(ProcessState.values());
        return STATES.filter(state -> state.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<ProcessState> STATES = Arrays.stream(ProcessState.values());
        Optional<String> result = STATES.filter(state -> state.getCode() == code)
            .map(ProcessState::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<ProcessState> getStateList() {
        return Arrays.asList(ProcessState.values());
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
