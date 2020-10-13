package com.diligrp.xtrade.upay.trade.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 免密支付协议类型
 *
 * @author: brenthuang
 * @date: 2020/10/10
 */
public enum ProtocolType implements IEnumType {
    ENTRY_FEE("进门收费", 30);

    private String name;
    private int code;

    ProtocolType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<ProtocolType> getType(int code) {
        Stream<ProtocolType> TYPES = Arrays.stream(ProtocolType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<ProtocolType> STATES = Arrays.stream(ProtocolType.values());
        Optional<String> result = STATES.filter(type -> type.getCode() == code)
            .map(ProtocolType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<ProtocolType> getTypeList() {
        return Arrays.asList(ProtocolType.values());
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
