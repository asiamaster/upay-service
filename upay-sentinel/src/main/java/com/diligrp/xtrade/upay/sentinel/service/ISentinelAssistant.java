package com.diligrp.xtrade.upay.sentinel.service;

import com.diligrp.xtrade.upay.sentinel.domain.ExecuteContext;
import com.diligrp.xtrade.upay.sentinel.domain.Passport;

/**
 * 风控引擎执行助手
 *
 * @author: brenthuang
 * @date: 2021/03/08
 */
public interface ISentinelAssistant {

    /**
     * 获取账户提现的执行上下文：日提现金额，日提现次数和月提现金额
     */
    ExecuteContext loadWithdrawExecuteContext(Passport passport);

    /**
     * 刷新账户提现的执行上下文：累计日提现金额，日提现次数和月提现金额
     */
    void refreshWithdrawExecuteContext(Passport passport);

    /**
     * 获取账户交易的执行上下文：日交易金额，日交易次数和月交易金额
     */
    ExecuteContext loadTradeExecuteContext(Passport passport);

    /**
     * 刷新账户交易的执行上下文：日交易金额，日交易次数和月交易金额
     */
    void refreshTradeExecuteContext(Passport passport);
}
