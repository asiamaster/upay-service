package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.shared.security.PasswordUtils;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IFundAccountDao;
import com.diligrp.xtrade.upay.core.domain.FundTransaction;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.service.IFundStreamEngine;
import com.diligrp.xtrade.upay.core.type.AccountState;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 账户/余额渠道服务实现
 */
@Service("accountChannelService")
public class AccountChannelServiceImpl implements IAccountChannelService {

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    /**
     * {@inheritDoc}
     *
     * 如果没有任何资金变动（资金收支或资金冻结）将抛出异常
     */
    @Override
    public TransactionStatus submit(IFundTransaction transaction) {
        Optional<FundTransaction> transactionOpt = transaction.fundTransaction();
        FundTransaction fundTransaction = transactionOpt.orElseThrow(
            () -> new PaymentChannelException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无效资金事务"));
        return fundStreamEngine.submit(fundTransaction);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FundAccount checkTradePermission(long accountId, String password, int maxPwdErrors) {
        AssertUtils.notEmpty(password, "password missed");
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(accountId);
        FundAccount account = accountOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        if (account.getState() != AccountState.NORMAL.getCode()) {
            throw new PaymentChannelException(ErrorCode.INVALID_ACCOUNT_STATE, "账户状态异常");
        }

        String encodedPwd = PasswordUtils.encrypt(password, account.getSecretKey());
        if (!ObjectUtils.equals(encodedPwd, account.getPassword())) {
            throw new PaymentChannelException(ErrorCode.INVALID_ACCOUNT_PASSWORD, "交易密码错误");
        }
        return account;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FundAccount checkTradePermission(long accountId) {
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(accountId);
        FundAccount account = accountOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        if (account.getState() != AccountState.NORMAL.getCode()) {
            throw new PaymentChannelException(ErrorCode.INVALID_ACCOUNT_STATE, "账户状态异常");
        }
        return account;
    }
}
