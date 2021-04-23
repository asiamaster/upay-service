package com.diligrp.xtrade.upay.sentinel.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 通道处理状态列表
 */
public enum PassportType implements IEnumType {
    FOR_DEPOSIT("充值权限", 0),

    FOR_WITHDRAW("提现权限", 1),

    FOR_TRADE("交易权限", 3);

    private String name;
    private int code;

    PassportType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<PassportType> getType(int code) {
        Stream<PassportType> TYPES = Arrays.stream(PassportType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<PassportType> STATES = Arrays.stream(PassportType.values());
        Optional<String> result = STATES.filter(type -> type.getCode() == code)
            .map(PassportType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<PassportType> getTypeList() {
        return Arrays.asList(PassportType.values());
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
