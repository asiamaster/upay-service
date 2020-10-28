package com.diligrp.xtrade.upay.core.service;

import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.model.UserAccount;

/**
 * 资金账户服务接口
 */
public interface IFundAccountService {
    /**
     * 创建资金账号
     */
    long createUserAccount(Long mchId, RegisterAccount account);

    /**
     * 冻结资金账号
     */
    void freezeUserAccount(Long accountId);

    /**
     * 解冻资金账号
     */
    void unfreezeUserAccount(Long accountId);

    /**
     * 注销资金账号
     */
    void unregisterUserAccount(Long mchId, Long accountId);

    /**
     * 根据账号ID查询用户账户
     */
    UserAccount findUserAccountById(Long accountId);

    /**
     * 根据账号ID查询资金账户
     */
    FundAccount findFundAccountById(Long accountId);

    /**
     * 重置账户交易密码
     */
    void resetTradePassword(long accountId, String password);
}
