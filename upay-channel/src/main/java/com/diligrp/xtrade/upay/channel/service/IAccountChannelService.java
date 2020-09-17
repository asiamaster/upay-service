package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenStatus;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.AccountFund;
import com.diligrp.xtrade.upay.core.model.FundAccount;

/**
 * 账户/余额渠道服务接口
 */
public interface IAccountChannelService {
    /**
     * 平台渠道注册资金账号
     */
    long registerFundAccount(Long mchId, RegisterAccount account);

    /**
     * 平台注销资金账号
     */
    void unregisterFundAccount(Long mchId, Long accountId);

    /**
     * 提交资金事务
     */
    TransactionStatus submit(IFundTransaction transaction);

    /**
     * 冻结平台账号
     */
    void freezeFundAccount(Long accountId);

    /**
     * 解冻平台账号
     */
    void unfreezeFundAccount(Long accountId);

    /**
     * 人工/系统冻结平台账户资金
     */
    FrozenStatus freezeAccountFund(FreezeFundDto request);

    /**
     * 人工/系统解冻平台账户资金
     */
    FrozenStatus unfreezeAccountFund(Long frozenId);

    /**
     * 查询平台账户资金信息
     */
    AccountFund queryAccountFund(Long accountId);

    /**
     * 检查交易权限：账户状态、交易密码
     */
    FundAccount checkTradePermission(long accountId, String password, int maxPwdErrors);

    /**
     * 检查交易权限：账户状态
     */
    FundAccount checkTradePermission(long accountId);

    /**
     * 重置交易密码: 不验证原密码
     */
    void resetTradePassword(long accountId, String password);
}
