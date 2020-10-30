package com.diligrp.xtrade.upay.boss.domain;

import com.diligrp.xtrade.shared.domain.Message;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.core.ErrorCode;

import java.util.List;

/**
 * 分页交易流水查询结果数据模型
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public class UserStatementResult extends Message<List<UserStatementDto>> {
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

    public static UserStatementResult success(long total, List<UserStatementDto> data, long income, long output) {
        UserStatementResult result = new UserStatementResult();
        result.setCode(CODE_SUCCESS);
        result.setTotal(total);
        result.setData(data);
        result.setIncome(income);
        result.setOutput(output);
        result.setMessage(MSG_SUCCESS);
        return result;
    }

    public static UserStatementResult failure(String message) {
        UserStatementResult result = new UserStatementResult();
        result.setCode(ErrorCode.SYSTEM_UNKNOWN_ERROR);
        result.setTotal(0);
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    public static UserStatementResult failure(int code, String message) {
        UserStatementResult result = new UserStatementResult();
        result.setCode(code);
        result.setTotal(0);
        result.setData(null);
        result.setMessage(message);
        return result;
    }
}
