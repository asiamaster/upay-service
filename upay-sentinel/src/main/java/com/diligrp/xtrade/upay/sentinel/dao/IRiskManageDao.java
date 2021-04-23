package com.diligrp.xtrade.upay.sentinel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.sentinel.model.GlobalPermission;
import com.diligrp.xtrade.upay.sentinel.model.UserPermission;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 支付风控管理数据访问层
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
@Repository("riskManageDao")
public interface IRiskManageDao extends MybatisMapperSupport {

    /**
     * 查找全局风控配置
     */
    Optional<GlobalPermission> findGlobalPermissionById(Long mchId);

    /**
     * 设置全局风控配置
     */
    void insertGlobalPermission(GlobalPermission permission);

    /**
     * 更新全局风控配置
     */
    int updateGlobalPermission(GlobalPermission permission);

    /**
     * 查找账户风控配置
     */
    Optional<UserPermission> findUserPermissionById(Long accountId);

    /**
     * 设置账户风控配置
     */
    void insertUserPermission(UserPermission permission);

    /**
     * 更新账户风控配置
     */
    int updateUserPermission(UserPermission permission);
}