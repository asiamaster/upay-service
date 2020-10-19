package com.diligrp.xtrade.upay.channel.domain;

/**
 * 交易流水汇总模型
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public class SumTradeStatement {
    // 总收入
    private long income;
    // 总支出
    private long output;

    public long getIncome() {
        return income;
    }

    public void setIncome(long income) {
        this.income = income;
    }

    public long getOutput() {
        return output;
    }

    public void setOutput(long output) {
        this.output = output;
    }
}
