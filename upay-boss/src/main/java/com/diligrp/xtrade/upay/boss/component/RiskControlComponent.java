package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.AccountId;
import com.diligrp.xtrade.upay.boss.domain.MerchantId;
import com.diligrp.xtrade.upay.boss.domain.Option;
import com.diligrp.xtrade.upay.boss.domain.RiskControl;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IUserAccountService;
import com.diligrp.xtrade.upay.core.type.Permission;
import com.diligrp.xtrade.upay.sentinel.domain.RiskControlContext;
import com.diligrp.xtrade.upay.sentinel.service.IRiskControlService;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 风控管理模块
 *
 * @author: brenthuang
 * @date: 2021/03/04
 */
@CallableComponent(id = "payment.sentinel.service")
public class RiskControlComponent {

    @Resource
    private IRiskControlService riskControlService;

    @Resource
    private IUserAccountService userAccountService;

    /**
     * 获取账户风控信息
     */
    public RiskControl loadGlobal(ServiceRequest<MerchantId> request) {
        MerchantId merchant = request.getData();
        AssertUtils.notNull(merchant.getMchId(), "mchId missed");

        RiskControl response = new RiskControl();
        RiskControlContext context = riskControlService.findGlobalRiskControl(merchant.getMchId());
        response.setDeposit(context.forDeposit());
        response.setWithdraw(context.forWithdraw());
        response.setTrade(context.forTrade());
        return response;
    }

    /**
     * 设置账户风控信息
     */
    public void setGlobal(ServiceRequest<RiskControl> request) {
        RiskControl rc = request.getData();
        AssertUtils.notNull(rc.getMchId(), "mchId missed");
        AssertUtils.notNull(rc.getDeposit(), "未设置充值风控信息");
        AssertUtils.notNull(rc.getDeposit().getMaxAmount(), "未设置单笔充值限额");
        AssertUtils.isTrue(rc.getDeposit().getMaxAmount() > 0, "单笔充值限额设置非法");
        AssertUtils.notNull(rc.getWithdraw(), "未设置提现风控信息");
        AssertUtils.notNull(rc.getWithdraw().getMaxAmount(), "未设置单笔提现限额");
        AssertUtils.isTrue(rc.getWithdraw().getMaxAmount() > 0, "单笔提现限额设置非法");
        AssertUtils.notNull(rc.getWithdraw().getDailyAmount(), "未设置日提现限额");
        AssertUtils.isTrue(rc.getWithdraw().getDailyAmount() > 0, "日提现限额设置非法");
        AssertUtils.notNull(rc.getWithdraw().getDailyTimes(), "未设置日提现次数");
        AssertUtils.isTrue(rc.getWithdraw().getDailyTimes() > 0, "日提现次数设置非法");
        AssertUtils.notNull(rc.getWithdraw().getMonthlyAmount(), "未设置月提现限额");
        AssertUtils.isTrue(rc.getWithdraw().getMonthlyAmount() > 0, "月提现限额设置非法");
        AssertUtils.notNull(rc.getTrade(), "未设置交易风控信息");
        AssertUtils.notNull(rc.getTrade().getMaxAmount(), "未设置单笔交易限额");
        AssertUtils.isTrue(rc.getTrade().getMaxAmount() > 0, "单笔交易限额设置非法");
        AssertUtils.notNull(rc.getTrade().getDailyAmount(), "未设置日交易限额");
        AssertUtils.isTrue(rc.getTrade().getDailyAmount() > 0, "日交易限额设置非法");
        AssertUtils.notNull(rc.getTrade().getDailyTimes(), "未设置日交易次数");
        AssertUtils.isTrue(rc.getTrade().getDailyTimes() > 0, "日交易次数设置非法");
        AssertUtils.notNull(rc.getTrade().getMonthlyAmount(), "未设置月交易限额");
        AssertUtils.isTrue(rc.getTrade().getMonthlyAmount() > 0, "月交易限额设置非法");

        RiskControlContext context = new RiskControlContext();
        context.forDeposit(rc.getDeposit());
        context.forWithdraw(rc.getWithdraw());
        context.forTrade(rc.getTrade());

        riskControlService.updateGlobalRiskControl(rc.getMchId(), context);
    }

    /**
     * 获取账户风控信息
     */
    public RiskControl load(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");

        RiskControl response = new RiskControl();
        // 获取账户已有权限
        UserAccount account = userAccountService.findUserAccountById(accountId.getAccountId());
        List<Integer> permissions = Permission.permissions(account.getPermission())
            .stream().map(Permission::getCode).collect(Collectors.toList());
        List<Option> allPermission = Permission.allSupportedPermissions().stream()
            .map(p -> Option.of(p.getCode(), p.getName())).collect(Collectors.toList());
        // 获取账户风控配置
        RiskControlContext context = riskControlService.findUserRiskControl(account);
        response.setPermission(permissions);
        response.setAllPermission(allPermission);
        response.setDeposit(context.forDeposit());
        response.setWithdraw(context.forWithdraw());
        response.setTrade(context.forTrade());
        return response;
    }

    /**
     * 设置账户风控信息
     */
    public void set(ServiceRequest<RiskControl> request) {
        RiskControl rc = request.getData();
        AssertUtils.notNull(rc.getAccountId(), "accountId missed");
        AssertUtils.notNull(rc.getPermission(), "permission missed, empty collection allowed");
        AssertUtils.notNull(rc.getWithdraw(), "未设置提现风控信息");
        AssertUtils.notNull(rc.getWithdraw().getDailyAmount(), "未设置日提现限额");
        AssertUtils.isTrue(rc.getWithdraw().getDailyAmount() > 0, "日提现限额设置非法");
        AssertUtils.notNull(rc.getWithdraw().getDailyTimes(), "未设置日提现次数");
        AssertUtils.isTrue(rc.getWithdraw().getDailyTimes() > 0, "日提现次数设置非法");
        AssertUtils.notNull(rc.getTrade(), "未设置交易风控信息");
        AssertUtils.notNull(rc.getTrade().getDailyAmount(), "未设置日交易限额");
        AssertUtils.isTrue(rc.getTrade().getDailyAmount() > 0, "日交易限额设置非法");
        AssertUtils.notNull(rc.getTrade().getDailyTimes(), "未设置日交易次数");
        AssertUtils.isTrue(rc.getTrade().getDailyTimes() > 0, "日交易次数设置非法");

        UserAccount account = userAccountService.findUserAccountById(rc.getAccountId());
        Permission[] permissions = rc.getPermission().stream().map(Permission::getPermission)
            .map(p -> p.orElseThrow(() -> new IllegalArgumentException("Invalid permission code"))).toArray(Permission[]::new);
        int permission = Permission.permissionMask(permissions);
        // 更新账户权限
        account.setPermission(permission);
        RiskControlContext context = new RiskControlContext();
        context.forWithdraw(rc.getWithdraw());
        context.forTrade(rc.getTrade());

        riskControlService.updateUserRiskControl(account, context);
    }
}
