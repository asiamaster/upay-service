package com.diligrp.xtrade.upay.sentinel.domain;

import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.type.Permission;
import com.diligrp.xtrade.upay.sentinel.exception.RiskControlException;

/**
 * 充值哨兵领域模型
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public class DepositSentinel extends Sentinel {
    // 单笔限额
    private Long maxAmount;

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public void override(DepositSentinel sentinel) {
        if (sentinel.maxAmount != null) {
            this.maxAmount = sentinel.maxAmount;
        }
    }

    @Override
    void checkPassport(Passport passport) {
        if (!Permission.hasPermission(passport.getPermission(), Permission.FOR_DEPOSIT)) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：用户账号无提现权限");
        }
        // 充值金额不能超过单笔充值限额
        if (maxAmount != null && passport.getAmount() > maxAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：充值金额超过单笔充值限额");
        }
    }

    @Override
    void admitPassport(Passport passport) {
        // Ignore it
    }
}
