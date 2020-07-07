package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 资金动作类型列表
 */
public enum ActionType implements IEnumType {

    INCOME("收入", 1),

    OUTGO("支出", 2);

    private String name;
    private int code;

    ActionType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<ActionType> getType(int code) {
        Stream<ActionType> TYPES = Arrays.stream(ActionType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<ActionType> TYPES = Arrays.stream(ActionType.values());
        Optional<String> result = TYPES.filter(status -> status.getCode() == code)
                .map(ActionType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static ActionType getByAmount(long amount) {
        return amount >= 0 ? ActionType.INCOME : ActionType.OUTGO;
    }

    public static List<ActionType> getTypeList() {
        return Arrays.asList(ActionType.values());
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
