package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenStatus;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.model.UserAccount;

/**
 * 账户/余额渠道服务接口
 */
public interface IAccountChannelService {
    /**
     * 平台渠道注册资金账号
     */
    long registerFundAccount(MerchantPermit merchant, RegisterAccount account);

    /**
     * 平台注销资金账号
     */
    void unregisterFundAccount(MerchantPermit merchant, Long accountId);

    /**
     * 提交资金事务, 数据库乐观锁且发生数据并发修改时进行重试
     */
    TransactionStatus submit(IFundTransaction transaction);

    /**
     * 提交资金事务, 数据库乐观锁且发生数据并发修改时"不会"进行重试
     */
    TransactionStatus submitOnce(IFundTransaction transaction);

    /**
     * 提交资金事务，建议提交商户账户操作时才使用此方法
     */
    TransactionStatus submitExclusively(IFundTransaction transaction);

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
     * 检查交易权限：账户状态、交易密码
     */
    UserAccount checkTradePermission(long accountId, String password, int maxPwdErrors);

    /**
     * 检查交易权限：账户状态
     */
    UserAccount checkTradePermission(long accountId);

    /**
     * 重置交易密码: 不验证原密码
     */
    void resetTradePassword(long accountId, String password);

    /**
     * 寿光市场需求：买卖家资金账户状态必须正常才允许交易，并且子账户交易时父账户状态必须正常
     */
    void checkAccountTradeState(UserAccount account);
}
