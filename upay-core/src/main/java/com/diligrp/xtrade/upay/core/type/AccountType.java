package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 资金账户类型
 */
public enum AccountType implements IEnumType {

    PERSONAL("个人", 1),

    PUBLIC("对公", 2),

    MERCHANT("商户", 3);

    private String name;
    private int code;

    AccountType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public boolean equalTo(int code) {
        return this.code == code;
    }

    public static Optional<AccountType> getType(int code) {
        Stream<AccountType> TYPES = Arrays.stream(AccountType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<AccountType> TYPES = Arrays.stream(AccountType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code)
            .map(AccountType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<AccountType> getTypeList() {
        return Arrays.asList(AccountType.values());
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
