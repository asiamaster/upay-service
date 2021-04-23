package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.AccountId;
import com.diligrp.xtrade.upay.boss.domain.CustomerBalance;
import com.diligrp.xtrade.upay.boss.domain.CustomerId;
import com.diligrp.xtrade.upay.boss.domain.FrozenId;
import com.diligrp.xtrade.upay.boss.domain.FrozenOrderDto;
import com.diligrp.xtrade.upay.boss.domain.FundBalance;
import com.diligrp.xtrade.upay.boss.domain.ListFrozen;
import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenAmount;
import com.diligrp.xtrade.upay.channel.domain.FrozenOrderQuery;
import com.diligrp.xtrade.upay.channel.domain.FrozenStatus;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.service.IFrozenOrderService;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 资金服务组件
 */
@CallableComponent(id = "payment.fund.service")
public class FundServiceComponent {

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IFrozenOrderService frozenOrderService;

    /**
     * 系统冻结资金
     */
    public FrozenStatus freeze(ServiceRequest<FreezeFundDto> request) {
        FreezeFundDto freezeFund = request.getData();
        AssertUtils.notNull(freezeFund.getAccountId(), "accountId missed");
        AssertUtils.notNull(freezeFund.getAmount(), "amount missed");
        freezeFund.setType(FrozenType.SYSTEM_FROZEN.getCode());
        return accountChannelService.freezeAccountFund(freezeFund);
    }

    /**
     * 系统解冻资金
     */
    public FrozenStatus unfreeze(ServiceRequest<FrozenId> request) {
        FrozenId frozenId = request.getData();
        AssertUtils.notNull(frozenId.getFrozenId(), "frozenId missed");
        return accountChannelService.unfreezeAccountFund(frozenId.getFrozenId());
    }

    /**
     * 查询账户余额
     */
    public FundBalance query(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");

        UserAccount account = fundAccountService.findUserAccountById(accountId.getAccountId());
        Long masterId = account.getParentId() == 0 ? account.getAccountId() : account.getParentId();

        FundAccount fundAccount = fundAccountService.findFundAccountById(masterId);
        return FundBalance.of(fundAccount.getAccountId(), fundAccount.getBalance(), fundAccount.getFrozenAmount());
    }

    /**
     * 查询账户余额(包含人工冻结和系统冻结金额)
     */
    public FundBalance queryEx(ServiceRequest<AccountId> request) {
        AccountId accountId = request.getData();
        AssertUtils.notNull(accountId.getAccountId(), "accountId missed");

        UserAccount account = fundAccountService.findUserAccountById(accountId.getAccountId());
        Long masterId = account.getParentId() == 0 ? account.getAccountId() : account.getParentId();

        FundAccount fundAccount = fundAccountService.findFundAccountById(masterId);
        FundBalance balance = FundBalance.of(fundAccount.getAccountId(), fundAccount.getBalance(), fundAccount.getFrozenAmount());
        Optional<FrozenAmount> frozenOpt = frozenOrderService.findFrozenAmount(masterId);
        FrozenAmount frozenAmount = frozenOpt.orElse(FrozenAmount.of(masterId, 0L, 0L));
        balance.setTradeFrozen(frozenAmount.getTradeFrozen());
        balance.setManFrozen(frozenAmount.getManFrozen());

        return balance;
    }

    /**
     * 分页查询冻结订单
     */
    public PageMessage<FrozenOrderDto> listFrozen(ServiceRequest<ListFrozen> request) {
        ListFrozen listFrozen = request.getData();
        AssertUtils.notNull(listFrozen.getAccountId(), "accountId missed");
        AssertUtils.isTrue(listFrozen.getPageNo() > 0, "invalid pageNum");
        AssertUtils.isTrue(listFrozen.getPageSize() > 0, "invalid pageSize");
        // 只查询系统冻结记录
        FrozenOrderQuery query = FrozenOrderQuery.of(listFrozen.getAccountId(), FrozenType.SYSTEM_FROZEN.getCode(),
            listFrozen.getState(), listFrozen.getStartTime(), listFrozen.getEndTime());
        query.from(listFrozen.getPageNo(), listFrozen.getPageSize());
        PageMessage<FrozenOrder> result = frozenOrderService.listFrozenOrders(query);
        // 转化查询结果
        List<FrozenOrderDto> frozenOrders = result.getData().stream().map(frozenOrder ->
            FrozenOrderDto.of(frozenOrder.getFrozenId(), frozenOrder.getAccountId(), frozenOrder.getAmount(),
            frozenOrder.getState(), frozenOrder.getExtension(), frozenOrder.getCreatedTime(),
            frozenOrder.getModifiedTime(), frozenOrder.getDescription())
        ).collect(Collectors.toList());
        return PageMessage.success(result.getTotal(), frozenOrders);
    }

    /**
     * 查询客户资金情况
     */
    public CustomerBalance customer(ServiceRequest<CustomerId> request) {
        CustomerId customer = request.getData();
        AssertUtils.notNull(customer.getCustomerId(), "customerId missed");
        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class);
        FundAccount fund = fundAccountService.sumCustomerFund(permit.getMerchant().parentMchId(), customer.getCustomerId());
        List<FundBalance> accountFunds =
            fundAccountService.listFundAccounts(permit.getMerchant().parentMchId(), customer.getCustomerId()).stream().map(
            fundAccount -> FundBalance.of(fundAccount.getAccountId(), fundAccount.getBalance(), fundAccount.getFrozenAmount()))
            .collect(Collectors.toList());
        return CustomerBalance.of(customer.getCustomerId(), fund.getBalance(), fund.getFrozenAmount(), accountFunds);
    }
}