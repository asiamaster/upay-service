package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.upay.core.domain.FundTransaction;

import java.util.Optional;

/**
 * 资金事务接口
 */
public interface IFundTransaction {
    /**
     * 资金收入
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 资金项说明
     */
    void income(long amount, int type, String typeName);

    /**
     * 资金支出
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 资金项说明
     */
    void outgo(long amount, int type, String typeName);

    /**
     * 资金冻结
     *
     * @param amount - 操作金额
     */
    void freeze(long amount);

    /**
     * 资金解冻
     *
     * @param amount - 操作金额
     */
    void unfreeze(long amount);

    /**
     * 获取资金事务
     */
    Optional<FundTransaction> fundTransaction();
}
