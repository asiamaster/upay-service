package com.diligrp.xtrade.upay.core.service;

import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.type.Permission;

import java.util.List;

/**
 * 用户账户服务接口
 *
 * @author: brenthuang
 * @date: 2021/01/26
 */
public interface IUserAccountService {

    /**
     * 查询用户账户
     */
    UserAccount findUserAccountById(Long accountId);

    /**
     * 获取用户账户权限
     *
     * @Deprecated 功能移至风控模块
     * @param accountId - 资金账户
     * @return 权限列表
     */
    List<Permission> loadUserAccountPermission(Long accountId);

    /**
     * 设置账户权限 @see AccountPermission
     *
     * @Deprecated 功能移至风控模块
     * @param permissions - 权限列表
     */
    void setUserAccountPermission(Long accountId, Permission... permissions);
}
