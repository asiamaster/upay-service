package com.diligrp.xtrade.upay.trade.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 交易类型列表
 */
public enum TradeType implements IEnumType {

    DEPOSIT("账户充值", 10),

    WITHDRAW("账户提现", 11),

    FEE("缴费", 12),

    AUTH_FEE("预授权缴费", 13),

    PRE_DEPOSIT("预存款", 14),

    DIRECT_TRADE("即时交易", 20),

    AUTH_TRADE("预授权交易", 21),

    VOUCH_TRADE("担保交易", 22),

    TRANSFER("账户转账", 23),

    BANK_DEPOSIT("银行圈存", 30),

    BANK_WITHDRAW("银行圈提", 31),

    CANCEL("交易撤销", 40),

    REFUND("交易退款", 41),

    CORRECT("交易冲正", 42);

    private String name;
    private int code;

    TradeType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<TradeType> getType(int code) {
        Stream<TradeType> TYPES = Arrays.stream(TradeType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<TradeType> STATES = Arrays.stream(TradeType.values());
        Optional<String> result = STATES.filter(type -> type.getCode() == code)
                .map(TradeType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<TradeType> getTypeList() {
        return Arrays.asList(TradeType.values());
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
