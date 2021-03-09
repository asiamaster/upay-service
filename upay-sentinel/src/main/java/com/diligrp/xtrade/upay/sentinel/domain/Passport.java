package com.diligrp.xtrade.upay.sentinel.domain;

import com.diligrp.xtrade.upay.sentinel.type.PassportType;

/**
 * 风控通行证领域模型
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public class Passport {
    // 通行证类型
    private PassportType type;
    // 账号ID
    private Long accountId;
    // 账号权限
    private Integer permission;
    // 发生金额
    private Long amount;

    public static Passport of(PassportType type, Long accountId, Integer permission, Long amount) {
        Passport passport = new Passport();
        passport.type = type;
        passport.accountId = accountId;
        passport.permission = permission;
        passport.amount = amount;
        return passport;
    }

    public static Passport ofDeposit(Long accountId, Integer permission, Long amount) {
        return of(PassportType.FOR_DEPOSIT, accountId, permission, amount);
    }

    public static Passport ofWithdraw(Long accountId, Integer permission, Long amount) {
        return of(PassportType.FOR_WITHDRAW, accountId, permission, amount);
    }

    public static Passport ofTrade(Long accountId, Integer permission, Long amount) {
        return of(PassportType.FOR_TRADE, accountId, permission, amount);
    }

    public PassportType getType() {
        return type;
    }

    public Long getAccountId() {
        return accountId;
    }

    public Integer getPermission() {
        return permission;
    }

    public void setPermission(Integer permission) {
        this.permission = permission;
    }

    public Long getAmount() {
        return amount;
    }
}
