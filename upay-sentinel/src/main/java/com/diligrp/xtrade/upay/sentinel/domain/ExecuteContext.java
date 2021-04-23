package com.diligrp.xtrade.upay.sentinel.domain;

/**
 * 风控引擎执行上下文
 *
 * 用于从Redis获取某个账号的日限额、日次数和月限额等执行参数
 *
 * @author: brenthuang
 * @date: 2021/03/08
 */
public class ExecuteContext {
    // 日操作金额
    private long dailyAmount;
    // 日操作次数
    private int dailyTimes;
    // 月操作金额
    private long monthlyAmount;

    public long getDailyAmount() {
        return dailyAmount;
    }

    public void setDailyAmount(long dailyAmount) {
        this.dailyAmount = dailyAmount;
    }

    public int getDailyTimes() {
        return dailyTimes;
    }

    public void setDailyTimes(int dailyTimes) {
        this.dailyTimes = dailyTimes;
    }

    public long getMonthlyAmount() {
        return monthlyAmount;
    }

    public void setMonthlyAmount(long monthlyAmount) {
        this.monthlyAmount = monthlyAmount;
    }
}
