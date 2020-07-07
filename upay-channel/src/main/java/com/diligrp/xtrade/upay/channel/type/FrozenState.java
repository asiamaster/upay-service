package com.diligrp.xtrade.upay.channel.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 资金冻结解冻状态列表
 */
public enum FrozenState implements IEnumType {

    FROZEN("冻结", 1),

    UNFROZEN("解冻", 2);

    private String name;
    private int code;

    FrozenState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<FrozenState> getType(int code) {
        Stream<FrozenState> STATES = Arrays.stream(FrozenState.values());
        return STATES.filter(state -> state.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<FrozenState> STATES = Arrays.stream(FrozenState.values());
        Optional<String> result = STATES.filter(state -> state.getCode() == code)
                .map(FrozenState::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<FrozenState> getStateList() {
        return Arrays.asList(FrozenState.values());
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
