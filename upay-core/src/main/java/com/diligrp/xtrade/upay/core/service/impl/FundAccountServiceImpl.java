package com.diligrp.xtrade.upay.core.service.impl;

import com.diligrp.xtrade.shared.security.PasswordUtils;
import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.shared.type.Gender;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IAccountFundDao;
import com.diligrp.xtrade.upay.core.dao.IFundAccountDao;
import com.diligrp.xtrade.upay.core.domain.AccountStateDto;
import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.exception.FundAccountException;
import com.diligrp.xtrade.upay.core.model.AccountFund;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.AccountState;
import com.diligrp.xtrade.upay.core.type.AccountType;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.type.UseFor;
import com.diligrp.xtrade.upay.core.util.AccountStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 资金账户服务实现
 */
@Service("fundAccountService")
public class FundAccountServiceImpl implements IFundAccountService {

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private IAccountFundDao accountFundDao;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public long createFundAccount(Long mchId, RegisterAccount account) {
        AccountType.getType(account.getType()).orElseThrow(
            () -> new FundAccountException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无效的账号类型"));
        UseFor.getType(account.getUseFor()).orElseThrow(
            () -> new FundAccountException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无效的业务用途"));
        if (account.getGender() != null) {
            Gender.getGender(account.getGender()).orElseThrow(
                () -> new FundAccountException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无效的性别"));
        }

        LocalDateTime when = LocalDateTime.now();
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FUND_ACCOUNT);
        long accountId = keyGenerator.nextId();
        String secretKey = PasswordUtils.generateSecretKey();
        String password = PasswordUtils.encrypt(account.getPassword(), secretKey);

        long parentId = account.getParentId() == null ? 0L : account.getParentId();
        FundAccount fundAccount = FundAccount.builder().customerId(account.getCustomerId()).accountId(accountId)
            .parentId(parentId).type(account.getType()).useFor(account.getUseFor()).code(account.getCode())
            .name(account.getName()).gender(account.getGender()).mobile(account.getMobile()).email(account.getEmail())
            .idCode(account.getIdCode()).address(account.getAddress()).password(password).secretKey(secretKey)
            .state(AccountState.NORMAL.getCode()).mchId(mchId).version(0).createdTime(when).build();
        // 创建子账户检查主资金账户状态
        fundAccount.ifChildAccount(act -> {
            Optional<FundAccount> masterOpt = fundAccountDao.findFundAccountById(account.getParentId());
            masterOpt.ifPresent(AccountStateMachine::registerSubAccountCheck);
            masterOpt.orElseThrow(() -> new FundAccountException(ErrorCode.OPERATION_NOT_ALLOWED, "主资金账户不存在"));
        });
        fundAccountDao.insertFundAccount(fundAccount);

        // 子账户无须创建账户资金，共享主账户资金
        fundAccount.ifMasterAccount(act -> {
            AccountFund accountFund = AccountFund.builder().accountId(accountId).balance(0L).frozenAmount(0L)
                .vouchAmount(0L).version(0).createdTime(when).build();
            accountFundDao.insertAccountFund(accountFund);
        });

        return accountId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void freezeFundAccount(Long accountId) {
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(accountId);
        accountOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::freezeAccountCheck);

        AccountStateDto accountState = AccountStateDto.of(accountId, AccountState.FROZEN.getCode(),
            LocalDateTime.now(), accountOpt.get().getVersion());
        Integer result = fundAccountDao.compareAndSetState(accountState);
        if (result == 0) {
            throw new FundAccountException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unfreezeFundAccount(Long accountId) {
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(accountId);
        accountOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::unfreezeAccountCheck);

        AccountStateDto accountState = AccountStateDto.of(accountId, AccountState.NORMAL.getCode(),
            LocalDateTime.now(), accountOpt.get().getVersion());
        Integer result = fundAccountDao.compareAndSetState(accountState);
        if (result == 0) {
            throw new FundAccountException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
    }

    /**
     * {@inheritDoc}
     *
     * 注销主账户时所有子账户必须为注销状态
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unregisterFundAccount(Long accountId) {
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(accountId);
        FundAccount account = accountOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::unregisterAccountCheck);
        Optional<AccountFund> fundOpt = accountFundDao.findAccountFundById(accountId);
        fundOpt.ifPresent(AccountStateMachine::unregisterFundCheck);

        // 不能注销存在子账号的资金账号
        account.ifMasterAccount(act -> {
            List<FundAccount> children = fundAccountDao.findFundAccountByParentId(account.getAccountId());
            children.stream().forEach(AccountStateMachine::unregisterAccountByChildCheck);
        });
        AccountStateDto accountState = AccountStateDto.of(accountId, AccountState.VOID.getCode(),
            LocalDateTime.now(), accountOpt.get().getVersion());
        Integer result = fundAccountDao.compareAndSetState(accountState);
        if (result == 0) {
            throw new FundAccountException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
    }

    @Override
    public FundAccount findFundAccountById(Long accountId) {
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(accountId);
        return accountOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
    }

    /**
     * {@inheritDoc}
     *
     * 乐观锁实现需Spring事务传播属性使用REQUIRES_NEW，数据库事务隔离级别READ_COMMITTED
     * 为了防止业务层事务的数据隔离级别和Mybatis的查询缓存干扰导致数据的重复读（无法读取到最新的数据记录），
     * 因此新启一个Spring事务（一个新数据库连接）并将数据隔离级别设置成READ_COMMITTED;
     * Mysql默认隔离级别为REPEATABLE_READ
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public AccountFund findAccountFundById(Long accountId) {
        Optional<AccountFund> fundOpt = accountFundDao.findAccountFundById(accountId);
        return fundOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "账号资金不存在"));
    }


    /**
     * {@inheritDoc}
     *
     * 重置密码不验证原密码
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void resetTradePassword(long accountId, String password) {
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(accountId);
        FundAccount account = accountOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::updateAccountCheck);
        String newPassword = PasswordUtils.encrypt(password, account.getSecretKey());
        account.setLoginPwd(newPassword);
        account.setPassword(newPassword);
        account.setModifiedTime(LocalDateTime.now());
        fundAccountDao.updateFundAccount(account);
    }
}