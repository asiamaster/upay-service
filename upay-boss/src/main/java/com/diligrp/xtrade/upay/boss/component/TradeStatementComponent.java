package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.ListTrade;
import com.diligrp.xtrade.upay.boss.domain.TradePermission;
import com.diligrp.xtrade.upay.boss.domain.TradeStatementDto;
import com.diligrp.xtrade.upay.boss.domain.TradeStatementResult;
import com.diligrp.xtrade.upay.channel.domain.SumTradeStatement;
import com.diligrp.xtrade.upay.channel.domain.TradeQuery;
import com.diligrp.xtrade.upay.channel.domain.TradeStatement;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.service.IChannelStatementService;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 交易流水服务组件
 */
@CallableComponent(id = "payment.statement.service")
public class TradeStatementComponent {

    @Resource
    private IChannelStatementService channelStatementService;

    /**
     * 账户状态和交易密码校验
     */
    public TradeStatementResult listTrades(ServiceRequest<ListTrade> request) {
        ListTrade listTrade = request.getData();
        AssertUtils.notNull(listTrade.getAccountId(), "accountId missed");
        AssertUtils.isTrue(listTrade.getPageNo() > 0, "invalid pageNo");
        AssertUtils.isTrue(listTrade.getPageSize() > 0, "invalid pageSize");
        LocalDate endDate = listTrade.getEndDate() != null ? listTrade.getEndDate().plusDays(1) : listTrade.getEndDate();
        TradeQuery query = TradeQuery.of(listTrade.getAccountId(), listTrade.getStartDate(), endDate);
        query.from(listTrade.getPageNo(), listTrade.getPageSize());

        PageMessage<TradeStatement> pagedTrades = channelStatementService.listTradeStatements(query);
        List<TradeStatementDto> trades = pagedTrades.getData().stream().map(
            trade -> TradeStatementDto.from(listTrade.getAccountId(), trade)).collect(Collectors.toList());
        if (pagedTrades.getTotal() > 0) {
            SumTradeStatement sum = channelStatementService.sumTradeStatements(query);
            return TradeStatementResult.success(pagedTrades.getTotal(), trades,
                sum == null ? 0 : sum.getIncome(), sum == null ? 0 : sum.getOutput());
        }
        return TradeStatementResult.success(0, Collections.emptyList(), 0, 0);
    }
}
