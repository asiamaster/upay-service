package com.diligrp.xtrade.upay.trade.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.trade.model.PaymentFee;
import com.diligrp.xtrade.upay.trade.model.TradeFee;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 支付费用数据访问层
 */
@Repository("paymentFeeDao")
public interface IPaymentFeeDao extends MybatisMapperSupport {
    void insertPaymentFees(List<PaymentFee> fees);

    List<PaymentFee> findPaymentFees(String paymentId);
}
