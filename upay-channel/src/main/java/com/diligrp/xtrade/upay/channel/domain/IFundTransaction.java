package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.upay.core.domain.FundTransaction;

import java.util.Optional;

/**
 * 资金事务接口
 */
public interface IFundTransaction {

    /**
     * 根据金额判断是资金收入还是资金支出
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 类型说明
     */
    default void consume(long amount, int type, String typeName, String description) {
        if (amount > 0) {
            income(amount, type, typeName, description);
        } else if (amount < 0) {
            outgo(amount, type, typeName, description);
        }
    }

    /**
     * 资金收入
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 类型说明
     * @param description - 费用描述
     */
    void income(long amount, int type, String typeName, String description);

    /**
     * 资金支出
     *
     * @param amount - 操作金额
     * @param type - 资金项类型
     * @param typeName - 类型说明
     * @param description - 费用描述
     */
    void outgo(long amount, int type, String typeName, String description);

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
