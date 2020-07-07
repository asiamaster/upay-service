package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.AccountId;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.AccountType;

import javax.annotation.Resource;

/**
 * 账号注册服务组件
 */
@CallableComponent(id = "payment.account.service")
public class AccountServiceComponent {

    @Resource
    private IFundAccountService fundAccountService;

    /**
     * 注册资金账号
     */
    public AccountId register(ServiceRequest<RegisterAccount> request) {
        RegisterAccount account = request.getData();
        // 进行入参校验
        AssertUtils.notNull(account.getCustomerId(), "customerId missed");
        AssertUtils.notNull(account.getType(), "type missed");
        AssertUtils.notNull(account.getUseFor(), "useFor missed");
        AssertUtils.notNull(account.getName(), "name missed");
        AssertUtils.notNull(account.getMobile(), "mobile missed");
        AssertUtils.notNull(account.getPassword(), "password missed");
        AssertUtils.isTrue(account.getType() != AccountType.MERCHANT.getCode(), "不能注册商户账号");

        ApplicationPermit application = request.getContext().getObject(ApplicationPermit.class.getName(), ApplicationPermit.class);
        long accountId = fundAccountService.createFundAccount(application.getMerchant().getMchId(), account);
        return AccountId.of(accountId);
    }

    /**
     * 冻结资金账号
     */
    public void freeze(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        fundAccountService.freezeFundAccount(accountId.getAccountId());
    }

    /**
     * 解冻资金账号
     */
    public void unfreeze(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        fundAccountService.unfreezeFundAccount(accountId.getAccountId());
    }

    /**
     * 注销资金账号
     */
    public void unregister(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");
        fundAccountService.unregisterFundAccount(accountId.getAccountId());
    }
}
