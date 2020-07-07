package com.diligrp.xtrade.upay.trade.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.trade.domain.PaymentStateDto;
import com.diligrp.xtrade.upay.trade.model.TradePayment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 交易支付数据访问层
 */
@Repository("tradePaymentDao")
public interface ITradePaymentDao extends MybatisMapperSupport {
    void insertTradePayment(TradePayment payment);

    Optional<TradePayment> findTradePaymentById(String paymentId);

    List<TradePayment> findTradePayments(String tradeId);

    Optional<TradePayment> findOneTradePayment(String tradeId);

    int compareAndSetState(PaymentStateDto frozenState);
}