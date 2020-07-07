package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.FundAccount;

/**
 * 账户/余额渠道服务接口
 */
public interface IAccountChannelService {
    /**
     * 提交资金事务
     */
    TransactionStatus submit(IFundTransaction transaction);

    /**
     * 检查交易权限：账户状态、交易密码
     */
    FundAccount checkTradePermission(long accountId, String password, int maxPwdErrors);

    /**
     * 检查交易权限：账户状态
     */
    FundAccount checkTradePermission(long accountId);
}
