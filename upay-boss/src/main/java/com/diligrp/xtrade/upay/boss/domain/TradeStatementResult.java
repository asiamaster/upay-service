package com.diligrp.xtrade.upay.boss.domain;

import com.diligrp.xtrade.shared.domain.Message;

import java.util.List;

/**
 * 分页交易流水查询结果数据模型
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public class TradeStatementResult extends Message<List<TradeStatementDto>> {
    // 总记录数
    private long total;
    // 总收入
    private long income;
    // 总支出
    private long output;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

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

    public static TradeStatementResult success(long total, List<TradeStatementDto> data, long income, long output) {
        TradeStatementResult result = new TradeStatementResult();
        result.setCode(CODE_SUCCESS);
        result.setTotal(total);
        result.setData(data);
        result.setIncome(income);
        result.setOutput(output);
        result.setMessage(MSG_SUCCESS);
        return result;
    }

    public static TradeStatementResult failure(String message) {
        TradeStatementResult result = new TradeStatementResult();
        result.setCode(CODE_FAILURE);
        result.setTotal(0);
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    public static TradeStatementResult failure(int code, String message) {
        TradeStatementResult result = new TradeStatementResult();
        result.setCode(code);
        result.setTotal(0);
        result.setData(null);
        result.setMessage(message);
        return result;
    }
}
