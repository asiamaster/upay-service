package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.ListUserStatement;
import com.diligrp.xtrade.upay.boss.domain.UserStatementResult;
import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.channel.domain.UserStatementFilter;
import com.diligrp.xtrade.upay.channel.domain.UserStatementQuery;
import com.diligrp.xtrade.upay.channel.service.IUserStatementService;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 客户账单服务组件
 */
@CallableComponent(id = "payment.statement.service")
public class UserStatementComponent {

    @Resource
    private IUserStatementService userStatementService;

    @Resource
    private IFundAccountService fundAccountService;

    /**
     * 分页查询客户账单明细
     */
    public UserStatementResult list(ServiceRequest<ListUserStatement> request) {
        ListUserStatement data = request.getData();
        AssertUtils.notNull(data.getAccountId(), "accountId missed");
        AssertUtils.isTrue(data.getPageNo() > 0, "invalid pageNo");
        AssertUtils.isTrue(data.getPageSize() > 0, "invalid pageSize");
        LocalDate endDate = data.getEndDate() != null ? data.getEndDate().plusDays(1) : data.getEndDate();
        UserStatementQuery query = UserStatementQuery.of(data.getType(), data.getAccountId(), data.getStartDate(), endDate);
        query.from(data.getPageNo(), data.getPageSize());

        UserAccount userAccount = fundAccountService.findUserAccountById(data.getAccountId());
        userAccount.ifChildAccount(account -> query.setAccountId(account.getParentId()));
        SumUserStatement sum = userStatementService.sumUserStatements(query);
        if (sum != null && sum.getTotal() > 0) {
            List<UserStatementDto> statements = userStatementService.listUserStatements(query);
            return UserStatementResult.success(sum.getTotal(), statements, sum.getIncome(), sum.getOutput());
        }
        return UserStatementResult.success(0, Collections.emptyList(), 0, 0);
    }

    /**
     * 根据交易号和资金账号查询"交易"账单(有且只有一条)
     */
    public UserStatementDto findOne(ServiceRequest<ListUserStatement> request) {
        ListUserStatement data = request.getData();
        AssertUtils.notEmpty(data.getTradeId(), "tradeId missed");
        AssertUtils.notNull(data.getAccountId(), "accountId missed");

        UserStatementFilter filter = UserStatementFilter.of(data.getTradeId(), data.getAccountId());
        UserAccount userAccount = fundAccountService.findUserAccountById(data.getAccountId());
        userAccount.ifChildAccount(account -> filter.setAccountId(account.getParentId()));
        return userStatementService.findUserStatement(filter);
    }

    /**
     * 根据交易号和资金账号查询"交易"账单(有且只有一条)
     */
    public List<UserStatementDto> listRefunds(ServiceRequest<ListUserStatement> request) {
        ListUserStatement data = request.getData();
        AssertUtils.notEmpty(data.getTradeId(), "tradeId missed");
        AssertUtils.notNull(data.getAccountId(), "accountId missed");

        UserStatementFilter filter = UserStatementFilter.of(data.getTradeId(), data.getAccountId());
        UserAccount userAccount = fundAccountService.findUserAccountById(data.getAccountId());
        userAccount.ifChildAccount(account -> filter.setAccountId(account.getParentId()));
        return userStatementService.listRefundStatements(filter);
    }
}
