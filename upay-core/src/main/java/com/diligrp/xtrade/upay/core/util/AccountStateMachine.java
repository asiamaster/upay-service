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
    public static void freezeAccountCheck(FundAccount account) {
        // 冻结资金账户状态检查
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已注销");
        }

        if (account.getState() == AccountState.FROZEN.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已被冻结");
        }
    }

    public static void unfreezeAccountCheck(FundAccount account) {
        // 解冻资金账户状态检查
        if (account.getState() == AccountState.VOID.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户已注销");
        }

        if (account.getState() != AccountState.FROZEN.getCode()) {
            throw new FundAccountException(ErrorCode.INVALID_ACCOUNT_STATE, "资金账户未被冻结");
        }
    }

    public static void unregisterAccountCheck(FundAccount account) {
        // 删除资金状态暂时无限制, "*" -> "注销"
    }

    public static void unregisterFundCheck(AccountFund fund) {
        if (fund.getBalance() > 0) {
            throw new FundAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "不能注销有余额的资金账户");
        }
    }
}
