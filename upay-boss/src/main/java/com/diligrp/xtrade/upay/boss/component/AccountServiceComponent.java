package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.AccountId;
import com.diligrp.xtrade.upay.boss.domain.MerchantId;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.type.AccountType;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;

/**
 * 账号注册服务组件
 */
@CallableComponent(id = "payment.account.service")
public class AccountServiceComponent {

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * 注册资金账号
     */
    public AccountId register(ServiceRequest<RegisterAccount> request) {
        RegisterAccount account = request.getData();
        // 进行入参校验
        AssertUtils.notNull(account.getCustomerId(), "customerId missed");
        AssertUtils.notNull(account.getType(), "type missed");
        AssertUtils.notNull(account.getUseFor(), "useFor missed");
        AssertUtils.notEmpty(account.getName(), "name missed");
        AssertUtils.notEmpty(account.getMobile(), "mobile missed");
        AssertUtils.notEmpty(account.getPassword(), "password missed");
        AssertUtils.isTrue(account.getType() != AccountType.MERCHANT.getCode(), "不能注册商户账号");

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        long accountId = accountChannelService.registerFundAccount(permit.getMerchant(), account);
        return AccountId.of(accountId);
    }

    /**
     * 冻结资金账号
     */
    public void freeze(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        accountChannelService.freezeFundAccount(accountId.getAccountId());
    }

    /**
     * 解冻资金账号
     */
    public void unfreeze(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        accountChannelService.unfreezeFundAccount(accountId.getAccountId());
    }

    /**
     * 注销资金账号
     */
    public void unregister(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        accountChannelService.unregisterFundAccount(permit.getMerchant(), accountId.getAccountId());
    }

    /**
     * 查询商户账户信息
     */
    public MerchantPermit merchant(ServiceRequest<MerchantId> request) {
        MerchantId merchant = request.getData();
        AssertUtils.notNull(merchant.getMchId(), "mchId missed");
        return accessPermitService.loadMerchantPermit(merchant.getMchId());
    }
}
