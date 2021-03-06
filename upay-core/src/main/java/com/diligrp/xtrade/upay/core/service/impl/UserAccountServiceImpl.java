package com.diligrp.xtrade.upay.core.service.impl;

import com.diligrp.xtrade.shared.security.PasswordUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IUserAccountDao;
import com.diligrp.xtrade.upay.core.exception.FundAccountException;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IUserAccountService;
import com.diligrp.xtrade.upay.core.type.Permission;
import com.diligrp.xtrade.upay.core.util.AccountStateMachine;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户账号服务实现
 *
 * @author: brenthuang
 * @date: 2021/01/26
 */
@Service("userAccountService")
public class UserAccountServiceImpl implements IUserAccountService {

    @Resource
    private IUserAccountDao userAccountDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount findUserAccountById(Long accountId) {
        Optional<UserAccount> accountOpt = userAccountDao.findUserAccountById(accountId);
        accountOpt.ifPresent(AccountStateMachine::voidAccountCheck);
        return accountOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
    }

    /**
     * {@inheritDoc}
     *
     * @Deprecated 功能移至风控模块
     */
    @Override
    public List<Permission> loadUserAccountPermission(Long accountId) {
        UserAccount userAccount = findUserAccountById(accountId);
        return Permission.permissions(userAccount.getPermission());
    }

    /**
     * {@inheritDoc}
     *
     * @Deprecated 功能移至风控模块
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void setUserAccountPermission(Long accountId, Permission... permissions) {
        findUserAccountById(accountId);
        int mask = Permission.permissionMask(permissions);
        UserAccount.Builder updated = UserAccount.builder().accountId(accountId).permission(mask).modifiedTime(LocalDateTime.now());
        userAccountDao.updateUserAccount(updated.build());
    }

    /**
     * {@inheritDoc}
     *
     * 提供密码时再进行密码校验, 不进行密码错误次数检查
     */
    @Override
    public UserAccount checkUserPassword(Long accountId, String password) {
        UserAccount account = findUserAccountById(accountId);
        if (ObjectUtils.isNotEmpty(password)) {
            String encodedPwd = PasswordUtils.encrypt(password, account.getSecretKey());
            if (!ObjectUtils.equals(encodedPwd, account.getPassword())) {
                throw new PaymentServiceException(ErrorCode.INVALID_ACCOUNT_PASSWORD, "账户交易密码不正确");
            }
        }
        return account;
    }
}
