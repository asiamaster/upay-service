package com.diligrp.xtrade.upay.channel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.channel.domain.FrozenStateDto;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 资金冻结订单数据访问
 */
@Repository("frozenOrderDao")
public interface IFrozenOrderDao extends MybatisMapperSupport {
    void insertFrozenOrder(FrozenOrder frozenOrder);

    Optional<FrozenOrder> findFrozenOrderById(Long frozenId);

    Optional<FrozenOrder> findFrozenOrderByPaymentId(String paymentId);

    int compareAndSetState(FrozenStateDto frozenState);
}