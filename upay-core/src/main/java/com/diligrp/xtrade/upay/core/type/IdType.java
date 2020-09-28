package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 账户业务用途
 */
public enum IdType implements IEnumType {

    ID_CARD("身份证", 1);

    private String name;
    private int code;

    IdType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<IdType> getType(int code) {
        Stream<IdType> TYPES = Arrays.stream(IdType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<IdType> TYPES = Arrays.stream(IdType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code)
            .map(IdType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<IdType> getTypeList() {
        return Arrays.asList(IdType.values());
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
