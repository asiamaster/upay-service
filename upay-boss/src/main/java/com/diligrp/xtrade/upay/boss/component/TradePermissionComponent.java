package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.AccountId;
import com.diligrp.xtrade.upay.boss.domain.Option;
import com.diligrp.xtrade.upay.boss.domain.TradePermission;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IUserAccountService;
import com.diligrp.xtrade.upay.core.type.Permission;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易权限服务组件
 */
@CallableComponent(id = "payment.permission.service")
public class TradePermissionComponent {

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IUserAccountService userAccountService;

    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * 账户状态和交易密码校验
     */
    public void password(ServiceRequest<TradePermission> request) {
        TradePermission permission = request.getData();

        AssertUtils.notNull(permission.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(permission.getPassword(), "password missed");

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class.getName(), ApplicationPermit.class);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(permit.getMerchant().getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        accountChannelService.checkTradePermission(permission.getAccountId(), permission.getPassword(), maxPwdErrors);
    }

    /**
     * 重置账户密码-不验证原密码
     */
    public void resetPwd(ServiceRequest<TradePermission> request) {
        TradePermission permission = request.getData();

        AssertUtils.notNull(permission.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(permission.getPassword(), "password missed");

        accountChannelService.resetTradePassword(permission.getAccountId(), permission.getPassword());
    }

    /**
     * 获取账户交易权限
     */
    public TradePermission load(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        TradePermission response = new TradePermission();

        List<Integer> permissions = userAccountService.loadUserAccountPermission(accountId.getAccountId())
            .stream().map(Permission::getCode).collect(Collectors.toList());
        List<Option> allPermission = Permission.allSupportedPermissions().stream()
            .map(p -> Option.of(p.getCode(), p.getName())).collect(Collectors.toList());
        response.setPermission(permissions);
        response.setAllPermission(allPermission);
        return response;
    }

    /**
     * 设置账户交易权限
     */
    public void set(ServiceRequest<TradePermission> request) {
        TradePermission permission = request.getData();
        AssertUtils.notNull(permission.getAccountId(), "accountId missed");
        AssertUtils.notNull(permission.getPermission(), "permission missed, empty collection allowed");
        Permission[] permissions = permission.getPermission().stream().map(Permission::getPermission)
            .map(p -> p.orElseThrow(() -> new IllegalArgumentException("Invalid permission code"))).toArray(Permission[]::new);
        userAccountService.setUserAccountPermission(permission.getAccountId(), permissions);
    }
}
