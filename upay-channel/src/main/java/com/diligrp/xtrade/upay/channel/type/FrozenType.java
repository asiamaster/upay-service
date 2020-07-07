package com.diligrp.xtrade.upay.channel.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 资金冻结类型
 */
public enum FrozenType implements IEnumType {
    // 内部交易冻结
    TRADE_FROZEN("交易冻结", 1),
    // 柜台人工冻结
    SYSTEM_FROZEN("系统冻结", 2);

    private String name;
    private int code;

    FrozenType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<FrozenType> getType(int code) {
        Stream<FrozenType> TYPES = Arrays.stream(FrozenType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<FrozenType> TYPES = Arrays.stream(FrozenType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code)
                .map(FrozenType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<FrozenType> getTypeList() {
        return Arrays.asList(FrozenType.values());
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
