package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.TradePermission;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;

import javax.annotation.Resource;

/**
 * 交易权限服务组件
 */
@CallableComponent(id = "payment.permission.service")
public class TradePermissionComponent {

    @Resource
    private IAccountChannelService accountChannelService;

    /**
     * 账户状态和交易密码校验
     */
    public void password(ServiceRequest<TradePermission> request) {
        TradePermission permission = request.getData();

        AssertUtils.notNull(permission.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(permission.getPassword(), "password missed");

        accountChannelService.checkTradePermission(permission.getAccountId(), permission.getPassword(), 5);
    }
}
