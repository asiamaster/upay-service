package com.diligrp.xtrade.upay.trade.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.trade.domain.PaymentStateDto;
import com.diligrp.xtrade.upay.trade.model.RefundPayment;
import com.diligrp.xtrade.upay.trade.model.TradePayment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 退款数据访问层
 */
@Repository("refundPaymentDao")
public interface IRefundPaymentDao extends MybatisMapperSupport {
    void insertRefundPayment(RefundPayment payment);

    Optional<RefundPayment> findRefundPaymentById(String paymentId);

    List<RefundPayment> findRefundPayments(String tradeId);
}