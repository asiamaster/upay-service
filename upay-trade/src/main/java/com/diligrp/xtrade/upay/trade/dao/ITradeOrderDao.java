package com.diligrp.xtrade.upay.trade.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.trade.domain.TradeStateDto;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 交易订单数据访问层
 */
@Repository("tradeOrderDao")
public interface ITradeOrderDao extends MybatisMapperSupport {
    void insertTradeOrder(TradeOrder tradeOrder);

    Optional<TradeOrder> findTradeOrderById(String tradeId);

    int compareAndSetState(TradeStateDto tradeState);
}