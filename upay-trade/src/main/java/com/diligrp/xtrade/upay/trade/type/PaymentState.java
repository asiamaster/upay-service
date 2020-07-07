package com.diligrp.xtrade.upay.trade.type;

import com.diligrp.xtrade.shared.type.IEnumType;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 支付状态列表
 */
public enum PaymentState implements IEnumType {

    PENDING("待处理", 1),

    PROCESSING("处理中", 2),

    SUCCESS("支付成功", 4),

    CANCELED("支付撤销", 6),

    FAILED("支付失败", 7);

    private String name;
    private int code;

    PaymentState(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public static Optional<PaymentState> getState(int code) {
        Stream<PaymentState> STATES = Arrays.stream(PaymentState.values());
        return STATES.filter(state -> state.getCode() == code).findFirst();
    }

    public static String getName(int code) {
        Stream<PaymentState> STATES = Arrays.stream(PaymentState.values());
        Optional<String> result = STATES.filter(state -> state.getCode() == code)
                .map(PaymentState::getName).findFirst();
        return result.isPresent() ? result.get() : null;
    }

    public static List<PaymentState> getStateList() {
        return Arrays.asList(PaymentState.values());
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
