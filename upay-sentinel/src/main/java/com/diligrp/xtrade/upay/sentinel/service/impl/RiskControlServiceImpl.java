package com.diligrp.xtrade.upay.sentinel.service.impl;

import com.diligrp.xtrade.shared.util.JsonUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IUserAccountDao;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.sentinel.dao.IRiskManageDao;
import com.diligrp.xtrade.upay.sentinel.domain.DepositSentinel;
import com.diligrp.xtrade.upay.sentinel.domain.RiskControlContext;
import com.diligrp.xtrade.upay.sentinel.domain.RiskControlEngine;
import com.diligrp.xtrade.upay.sentinel.domain.TradeSentinel;
import com.diligrp.xtrade.upay.sentinel.domain.WithdrawSentinel;
import com.diligrp.xtrade.upay.sentinel.exception.RiskControlException;
import com.diligrp.xtrade.upay.sentinel.model.GlobalPermission;
import com.diligrp.xtrade.upay.sentinel.model.UserPermission;
import com.diligrp.xtrade.upay.sentinel.service.ISentinelAssistant;
import com.diligrp.xtrade.upay.sentinel.service.IRiskControlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 风险控制服务实现
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
@Service("riskControlService")
public class RiskControlServiceImpl implements IRiskControlService {

    @Resource
    private IRiskManageDao riskManageDao;

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private ISentinelAssistant executeAssistant;

    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * {@inheritDoc}
     *
     * 风控检查时用于加载风控引擎
     */
    @Override
    public RiskControlEngine loadRiskControlEngine(UserAccount account) {
        RiskControlEngine rc = new RiskControlEngine();
        // 由于用户风控的参数是全局风控参数的子级，因此每次风控检查都需要同时加载全局配置和用户配置
        // 加载全局风控配置, 如无则表示商户未开启风控检查
        Optional<GlobalPermission> globalOpt = riskManageDao.findGlobalPermissionById(account.getMchId());
        globalOpt.ifPresent(global -> {
            rc.initEngine(executeAssistant);
            DepositSentinel deposit = JsonUtils.fromJsonString(global.getDeposit(), DepositSentinel.class);
            WithdrawSentinel withdraw = JsonUtils.fromJsonString(global.getWithdraw(), WithdrawSentinel.class);
            TradeSentinel trade = JsonUtils.fromJsonString(global.getTrade(), TradeSentinel.class);
            // 加载用户风控配置，如存在则覆盖部分全局配置
            Optional<UserPermission> userOpt = riskManageDao.findUserPermissionById(account.getAccountId());
            userOpt.ifPresent(user -> {
                deposit.override(JsonUtils.fromJsonString(user.getDeposit(), DepositSentinel.class));
                withdraw.override(JsonUtils.fromJsonString(user.getWithdraw(), WithdrawSentinel.class));
                trade.override(JsonUtils.fromJsonString(user.getTrade(), TradeSentinel.class));
            });
            rc.forDeposit(deposit);
            rc.forWithdraw(withdraw);
            rc.forTrade(trade);
        });

        return rc;
    }

    /**
     * {@inheritDoc}
     *
     * 用于显示商户全局风控配置信息
     */
    @Override
    public RiskControlContext findGlobalRiskControl(Long mchId) {
        // 确保商户信息已注册
        accessPermitService.loadMerchantPermit(mchId);
        GlobalPermission global = riskManageDao.findGlobalPermissionById(mchId)
            .orElseThrow(() -> new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "商户未启用风控功能"));
        RiskControlContext rc = new RiskControlContext();
        rc.forDeposit(JsonUtils.fromJsonString(global.getDeposit(), DepositSentinel.class));
        rc.forWithdraw(JsonUtils.fromJsonString(global.getWithdraw(), WithdrawSentinel.class));
        rc.forTrade(JsonUtils.fromJsonString(global.getTrade(), TradeSentinel.class));

        return rc;
    }

    /**
     * {@inheritDoc}
     *
     * 用于更新商户全局风控配置信息
     */
    @Override
    public void updateGlobalRiskControl(Long mchId, RiskControlContext context) {
        // 确保商户信息已注册
        accessPermitService.loadMerchantPermit(mchId);
        GlobalPermission global = riskManageDao.findGlobalPermissionById(mchId)
            .orElseThrow(() -> new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "商户未启用风控功能"));
        global.setDeposit(JsonUtils.toJsonString(context.forDeposit()));
        global.setWithdraw(JsonUtils.toJsonString(context.forWithdraw()));
        global.setTrade(JsonUtils.toJsonString(context.forTrade()));
        global.setModifiedTime(LocalDateTime.now());
        riskManageDao.updateGlobalPermission(global);
    }

    /**
     * {@inheritDoc}
     *
     * 用于显示用户风控信息
     */
    @Override
    public RiskControlContext findUserRiskControl(UserAccount account) {
        return loadRiskControlEngine(account).context();
    }

    /**
     * {@inheritDoc}
     *
     * 用于更新用户风控配置
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateUserRiskControl(UserAccount account, RiskControlContext context) {
        LocalDateTime when = LocalDateTime.now();
        Optional<GlobalPermission> globalOpt = riskManageDao.findGlobalPermissionById(account.getMchId());
        globalOpt.orElseThrow(() -> new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "商户未启用风控功能"));
        // 更新账户权限
        UserAccount.Builder updated = UserAccount.builder().accountId(account.getAccountId())
            .permission(account.getPermission()).modifiedTime(LocalDateTime.now());
        userAccountDao.updateUserAccount(updated.build());

        // 账户级风控无充值风控信息，默认为"空"设置
        UserPermission permission = UserPermission.builder().accountId(account.getAccountId())
            .deposit(JsonUtils.toJsonString(new DepositSentinel())).withdraw(JsonUtils.toJsonString(context.forWithdraw()))
            .trade(JsonUtils.toJsonString(context.forTrade())).createdTime(when).modifiedTime(when).build();
        // 无风控信息则新增
        int result = riskManageDao.updateUserPermission(permission);
        if (result == 0) {
            riskManageDao.insertUserPermission(permission);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISentinelAssistant getSentinelAssistant() {
        return executeAssistant;
    }
}
