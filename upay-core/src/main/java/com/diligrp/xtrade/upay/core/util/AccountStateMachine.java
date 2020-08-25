package com.diligrp.xtrade.upay.core.util;

import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.exception.FundAccountException;
import com.diligrp.xtrade.upay.core.model.AccountFund;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.type.AccountState;

/**
 * 资金账户状态机
 */
public final class AccountStateMachine {
    /**
     * 校验是否可以冻结资金账户
     */
    public static void freezeAccountCheck(FundAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已注销");
        }

        if (account.getState() == AccountState.FROZEN.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已被冻结");
        }
    }

    /**
     * 校验是否可以解冻资金账户
     */
    public static void unfreezeAccountCheck(FundAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已注销");
        }

        if (account.getState() != AccountState.FROZEN.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户未被冻结");
        }
    }

    /**
     * 校验是否可以注销资金账号
     */
    public static void unregisterAccountCheck(FundAccount account) {
        if (account.getState() == AccountState.FROZEN.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已冻结");
        }
    }

    /**
     * 校验是否可以修改账号信息
     */
    public static void updateAccountCheck(FundAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已注销");
        }
    }

    /**
     * 校验是否可以解冻或解冻资金
     */
    public static void frozenFundCheck(FundAccount account) {
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已注销");
        }
        if (account.getState() == AccountState.FROZEN.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已冻结");
        }
    }

    /**
     * 校验是否可以注销账号资金
     */
    public static void unregisterFundCheck(AccountFund fund) {
        if (fund.getBalance() > 0) {
            throw new FundAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "不能注销有余额的资金账户");
        }
    }
}
