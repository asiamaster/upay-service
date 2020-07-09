package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.AccountId;
import com.diligrp.xtrade.upay.boss.domain.FrozenId;
import com.diligrp.xtrade.upay.boss.domain.FundBalance;
import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.core.model.AccountFund;

import javax.annotation.Resource;

/**
 * 资金服务组件
 */
@CallableComponent(id = "payment.fund.service")
public class FundServiceComponent {

    @Resource
    private IAccountChannelService accountChannelService;

    /**
     * 系统冻结资金
     */
    public FrozenId freeze(ServiceRequest<FreezeFundDto> request) {
        FreezeFundDto freezeFund = request.getData();
        AssertUtils.notNull(freezeFund.getAccountId(), "accountId missed");
        AssertUtils.notNull(freezeFund.getBusinessId(), "businessId missed");
        AssertUtils.notNull(freezeFund.getAmount(), "amount missed");
        freezeFund.setType(FrozenType.SYSTEM_FROZEN.getCode());
        Long id = accountChannelService.freezeAccountFund(freezeFund);
        return FrozenId.of(id);
    }

    /**
     * 系统解冻资金
     */
    public void unfreeze(ServiceRequest<FrozenId> request) {
        FrozenId frozenId = request.getData();
        AssertUtils.notNull(frozenId.getFrozenId(), "frozenId missed");
        accountChannelService.unfreezeAccountFund(frozenId.getFrozenId());
    }

    /**
     * 查询账户余额
     */
    public FundBalance query(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");

        AccountFund accountFund = accountChannelService.queryAccountFund(accountId.getAccountId());
        return FundBalance.of(accountFund.getAccountId(), accountFund.getBalance(), accountFund.getFrozenAmount());
    }
}