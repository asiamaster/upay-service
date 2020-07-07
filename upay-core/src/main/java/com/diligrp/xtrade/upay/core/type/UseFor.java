package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 账户业务用途
 */
public enum UseFor implements IEnumType {

    FOR_TRADE("交易账户", 1),

    FOR_FEE("缴费账户", 2),

    FOR_DEPOSIT("预存账户", 3),

    FOR_PROFIT("收益账户", 10),

    FOR_VOUCH("担保账户", 11),

    FOR_PLEDGE("押金账户", 12);

    private String name;
    private int code;

    UseFor(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<UseFor> getType(int code) {
        Stream<UseFor> TYPES = Arrays.stream(UseFor.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<UseFor> TYPES = Arrays.stream(UseFor.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code)
            .map(UseFor::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<UseFor> getTypeList() {
        return Arrays.asList(UseFor.values());
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
