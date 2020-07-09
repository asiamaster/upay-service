package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.shared.security.PasswordUtils;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.service.IFrozenOrderService;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IFundAccountDao;
import com.diligrp.xtrade.upay.core.domain.FundTransaction;
import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.exception.FundAccountException;
import com.diligrp.xtrade.upay.core.model.AccountFund;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.service.IFundStreamEngine;
import com.diligrp.xtrade.upay.core.type.AccountState;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * 平台渠道服务实现
 */
@Service("accountChannelService")
public class AccountChannelServiceImpl implements IAccountChannelService {

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IFundStreamEngine fundStreamEngine;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IFrozenOrderService frozenOrderService;

    /**
     * {@inheritDoc}
     *
     * 注册平台账户需指定商户
     */
    @Override
    public long registerFundAccount(Long mchId, RegisterAccount account) {
        return fundAccountService.createFundAccount(mchId, account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterFundAccount(Long accountId) {
        fundAccountService.unregisterFundAccount(accountId);
    }

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
    public void freezeFundAccount(Long accountId) {
        fundAccountService.freezeFundAccount(accountId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unfreezeFundAccount(Long accountId) {
        fundAccountService.unfreezeFundAccount(accountId);
    }

    /**
     * {@inheritDoc}
     *
     * 人工/系统冻结使用
     */
    @Override
    public long freezeAccountFund(FreezeFundDto request) {
        return frozenOrderService.freeze(request);
    }

    /**
     * {@inheritDoc}
     *
     * 人工/系统解冻资金使用
     */
    @Override
    public void unfreezeAccountFund(Long frozenId) {
        frozenOrderService.unfreeze(frozenId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountFund queryAccountFund(Long accountId) {
        Optional<AccountFund> accountFund = fundAccountService.findAccountFundById(accountId);;
        return accountFund.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
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
