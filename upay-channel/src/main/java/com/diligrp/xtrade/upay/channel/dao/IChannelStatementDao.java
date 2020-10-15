package com.diligrp.xtrade.upay.channel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.channel.domain.TradeQuery;
import com.diligrp.xtrade.upay.channel.domain.TradeStatement;
import com.diligrp.xtrade.upay.channel.domain.SumTradeStatement;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 账户余额快照数据访问层
 *
 * @author: brenthuang
 * @date: 2020/07/29
 */
@Repository("channelStatementDao")
public interface IChannelStatementDao extends MybatisMapperSupport {
    /**
     * 查询客户交易明细
     */
    List<TradeStatement> listTradeStatements(TradeQuery query);

    /**
     * 查询客户交易总记录数
     */
    long countTradeStatements(TradeQuery query);

    /**
     * 查询客户总收入和总支出
     */
    SumTradeStatement sumTradeStatements(TradeQuery query);
}