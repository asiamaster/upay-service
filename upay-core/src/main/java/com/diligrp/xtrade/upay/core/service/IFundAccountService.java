package com.diligrp.xtrade.upay.core.service;

import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.model.AccountFund;

import java.util.Optional;

/**
 * 资金账户服务接口
 */
public interface IFundAccountService {
    /**
     * 创建资金账号
     */
    long createFundAccount(Long mchId, RegisterAccount account);

    /**
     * 冻结资金账号
     */
    void freezeFundAccount(Long accountId);

    /**
     * 解冻资金账号
     */
    void unfreezeFundAccount(Long accountId);

    /**
     * 注销资金账号
     */
    void unregisterFundAccount(Long accountId);

    /**
     * 根据账号ID查询账户资金
     */
    Optional<AccountFund> findAccountFundById(Long accountId);
}
