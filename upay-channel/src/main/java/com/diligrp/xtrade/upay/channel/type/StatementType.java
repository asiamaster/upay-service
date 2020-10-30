package com.diligrp.xtrade.upay.channel.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 业务账单类型
 */
public enum StatementType implements IEnumType {

    DEPOSIT("充值", 10),

    WITHDRAW("提现", 20),

    PAY_FEE("缴费", 30),

    TRADE("交易", 40),

    REFUND_FEE("退费", 50),

    TRANSFER("转账", 60),

    REFUND("退款", 70);

    private String name;
    private int code;

    StatementType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<StatementType> getType(int code) {
        Stream<StatementType> TYPES = Arrays.stream(StatementType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<StatementType> TYPES = Arrays.stream(StatementType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code)
                .map(StatementType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<StatementType> getTypeList() {
        return Arrays.asList(StatementType.values());
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
