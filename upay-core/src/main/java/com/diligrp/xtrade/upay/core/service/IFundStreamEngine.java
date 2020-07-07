package com.diligrp.xtrade.upay.core.service;

import com.diligrp.xtrade.upay.core.domain.FundTransaction;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;

/**
 * 资金流引擎接口
 */
public interface IFundStreamEngine {
    /**
     * 提交资金事务: 操作资金余额和添加资金流水、冻结和解冻资金
     */
    TransactionStatus submit(FundTransaction transaction);
}
