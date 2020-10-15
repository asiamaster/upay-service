package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.upay.channel.dao.IChannelStatementDao;
import com.diligrp.xtrade.upay.channel.domain.SumTradeStatement;
import com.diligrp.xtrade.upay.channel.domain.TradeQuery;
import com.diligrp.xtrade.upay.channel.domain.TradeStatement;
import com.diligrp.xtrade.upay.channel.service.IChannelStatementService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 渠道流水服务接口
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
@Service("channelStatementService")
public class ChannelStatementServiceImpl implements IChannelStatementService {

    @Resource
    private IChannelStatementDao channelStatementDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageMessage<TradeStatement> listTradeStatements(TradeQuery query) {
        long total = channelStatementDao.countTradeStatements(query);
        List<TradeStatement> trades = Collections.emptyList();
        if (total > 0) {
            trades = channelStatementDao.listTradeStatements(query);
        }
        return PageMessage.success(total, trades);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SumTradeStatement sumTradeStatements(TradeQuery query) {
        return channelStatementDao.sumTradeStatements(query);
    }
}
