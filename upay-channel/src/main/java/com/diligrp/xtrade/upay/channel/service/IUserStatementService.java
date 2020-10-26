package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.upay.channel.domain.SumTradeStatement;
import com.diligrp.xtrade.upay.channel.domain.TradeQuery;
import com.diligrp.xtrade.upay.channel.domain.TradeStatement;

/**
 * 渠道流水服务接口
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public interface IUserStatementService {
    /**
     * 分页查询客户交易明细
     */
    PageMessage<TradeStatement> listTradeStatements(TradeQuery query);

    /**
     * 查询客户总收入和总支出
     */
    SumTradeStatement sumTradeStatements(TradeQuery query);
}
