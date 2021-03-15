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
public class WithdrawSentinel extends Sentinel {
    // 单笔限额
    private Long maxAmount;
    // 日限额
    private Long dailyAmount;
    // 日次数
    private Integer dailyTimes;
    // 月限额
    private Long monthlyAmount;

    public Long getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Long maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Long getDailyAmount() {
        return dailyAmount;
    }

    public void setDailyAmount(Long dailyAmount) {
        this.dailyAmount = dailyAmount;
    }

    public Integer getDailyTimes() {
        return dailyTimes;
    }

    public void setDailyTimes(Integer dailyTimes) {
        this.dailyTimes = dailyTimes;
    }

    public Long getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(Long monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }

    public void override(WithdrawSentinel sentinel) {
        if (sentinel.maxAmount != null) {
            this.maxAmount = sentinel.maxAmount;
        }
        if (sentinel.dailyAmount != null) {
            this.dailyAmount = sentinel.dailyAmount;
        }
        if (sentinel.dailyTimes != null) {
            this.dailyTimes = sentinel.dailyTimes;
        }
        if (sentinel.monthlyAmount != null) {
            this.monthlyAmount = sentinel.monthlyAmount;
        }
    }

    @Override
    void checkPassport(Passport passport) {
        if (!Permission.hasPermission(passport.getPermission(), Permission.FOR_WITHDRAW)) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：用户账号无提现权限");
        }
        // 提现金额不能超过单笔提现限额
        if (maxAmount != null && passport.getAmount() > maxAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：提现金额超过单笔提现限额");
        }
        // 获取执行上下文: 账户日提现金额，日提现次数和月提现金额
        ExecuteContext context = sentinelAssistant.loadWithdrawExecuteContext(passport);
        if(dailyAmount != null && context.getDailyAmount() + passport.getAmount() > dailyAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计提现金额超过日提现限额");
        }
        if (dailyTimes != null && context.getDailyTimes() + 1 > dailyTimes) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计提现次数超过日提现次数");
        }
        if(monthlyAmount != null && context.getMonthlyAmount() + passport.getAmount() > monthlyAmount) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "风控提示：累计提现金额超过月提现限额");
        }
    }

    @Override
    void admitPassport(Passport passport) {
        sentinelAssistant.refreshWithdrawExecuteContext(passport);
    }
}
