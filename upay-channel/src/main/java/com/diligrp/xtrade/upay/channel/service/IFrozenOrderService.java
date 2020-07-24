package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenOrderQuery;
import com.diligrp.xtrade.upay.channel.domain.FrozenStatus;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;

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

    /**
     * 分页查询冻结/解冻订单
     */
    PageMessage<FrozenOrder> listFrozenOrders(FrozenOrderQuery query);
}
