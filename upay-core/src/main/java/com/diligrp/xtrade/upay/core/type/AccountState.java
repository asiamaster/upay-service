package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 账户状态列表
 */
public enum AccountState implements IEnumType {

    NORMAL("正常", 1),

    FROZEN("冻结", 2),

    VOID("注销", 3);

    private String name;
    private int code;

    AccountState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<AccountState> getState(int code) {
        Stream<AccountState> STATES = Arrays.stream(AccountState.values());
        return STATES.filter(status -> status.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<AccountState> STATES = Arrays.stream(AccountState.values());
        Optional<String> result = STATES.filter(status -> status.getCode() == code)
                .map(AccountState::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<AccountState> getStatusList() {
        return Arrays.asList(AccountState.values());
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
