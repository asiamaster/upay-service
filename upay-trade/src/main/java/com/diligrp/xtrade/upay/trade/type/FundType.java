package com.diligrp.xtrade.upay.trade.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 资金项类型列表
 */
public enum FundType implements IEnumType {

    FUND("账户资金", 0),

    POUNDAGE("手续费", 1),

    COST("工本费", 2),

    PLEDGE("押金", 3);

    private String name;
    private int code;

    FundType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<FundType> getType(int code) {
        Stream<FundType> TYPES = Arrays.stream(FundType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static Optional<FundType> getFee(int code) {
        Optional<FundType> fundTypeOpt = getType(code);
        return fundTypeOpt.filter(type -> type != FUND);
    }

    public static String getName(int code) {
        Stream<FundType> STATES = Arrays.stream(FundType.values());
        Optional<String> result = STATES.filter(type -> type.getCode() == code)
                .map(FundType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<FundType> getTypeList() {
        return Arrays.asList(FundType.values());
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
