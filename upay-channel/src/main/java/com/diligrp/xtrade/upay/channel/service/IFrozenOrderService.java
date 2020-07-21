package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenStatus;

/**
 * 资金冻结/解冻订单服务接口
 */
public interface IFrozenOrderService {
    /**
     * 资金冻结
     */
    FrozenStatus freeze(FreezeFundDto request);

    /**
     * 资金解冻
     */
    FrozenStatus unfreeze(Long frozenId);
}
