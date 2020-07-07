package com.diligrp.xtrade.upay.channel.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 支持的渠道
 */
public enum ChannelType implements IEnumType {

    ACCOUNT("账户渠道", 1),

    CASH("现金渠道", 2),

    POS("POS渠道", 3),

    E_BANK("网银渠道", 4);

    private String name;
    private int code;

    ChannelType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<ChannelType> getType(int code) {
        Stream<ChannelType> TYPES = Arrays.stream(ChannelType.values());
        return TYPES.filter(type -> type.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<ChannelType> TYPES = Arrays.stream(ChannelType.values());
        Optional<String> result = TYPES.filter(type -> type.getCode() == code)
                .map(ChannelType::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<ChannelType> getTypeList() {
        return Arrays.asList(ChannelType.values());
    }

    /**
     * 判断渠道是否可用于充值业务
     */
    public static boolean forDeposit(int code) {
        return code == CASH.getCode() || code == POS.getCode() || code == E_BANK.getCode();
    }

    /**
     * 判断渠道是否可用于提现业务
     */
    public static boolean forWithdraw(int code) {
        return code == CASH.getCode() || code == E_BANK.getCode();
    }

    /**
     * 判断渠道是否可用于缴费业务
     */
    public static boolean forFee(int code) {
        return code == CASH.getCode() || code == ACCOUNT.getCode();
    }

    /**
     * 判断渠道是否可用于"即时交易", "预授权交易"和"转账"业务
     */
    public static boolean forTrade(int code) {
        return code == ACCOUNT.getCode();
    }

    /**
     * 判断渠道是否可用于"预授权缴费"业务
     */
    public static boolean forPreAuthFee(int code) {
        return code == ACCOUNT.getCode();
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
