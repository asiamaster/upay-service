package com.diligrp.xtrade.upay.core.service;

import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.model.UserAccount;

import java.util.List;

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
     * 冻结资金账号 - 独立的数据库事务
     */
    void freezeUserAccountNow(Long accountId);

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
     * 悲观锁锁定资金账号防止数据并发修改
     */
    FundAccount lockFundAccountById(Long accountId);

    /**
     * 重置账户交易密码
     */
    void resetTradePassword(long accountId, String password);

    /**
     * 查询客户账户资金汇总信息
     */
    FundAccount sumCustomerFund(Long customerId);

    /**
     * 查询客户资金账户列表
     */
    List<FundAccount> listFundAccounts(Long customerId);
}
