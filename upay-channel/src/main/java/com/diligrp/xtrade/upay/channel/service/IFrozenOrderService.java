package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;

/**
 * 资金冻结/解冻订单服务接口
 */
public interface IFrozenOrderService {
    /**
     * 资金冻结
     */
    long freeze(FreezeFundDto request);

    /**
     * 资金解冻
     */
    void unfreeze(Long frozenId);
}
