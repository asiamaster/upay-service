package com.diligrp.xtrade.upay.trade.service;

import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.trade.domain.ProtocolQuery;
import com.diligrp.xtrade.upay.trade.domain.ProtocolRegister;
import com.diligrp.xtrade.upay.trade.model.UserProtocol;

/**
 * 免密支付协议服务接口
 *
 * @author: brenthuang
 * @date: 2020/10/12
 */
public interface IPaymentProtocolService {
    /**
     * 注册免密支付协议
     *
     * @param request - 注册申请
     * @return 用户协议
     */
    UserProtocol registerUserProtocol(ProtocolRegister request);

    /**
     * 查询免密支付协议
     *
     * @param request - 查询申请
     * @return 用户协议
     */
    UserProtocol queryUserProtocol(ProtocolQuery request);

    /**
     * 检查免密支付权限
     */
    UserAccount checkProtocolPermission(long accountId, long protocolId, long amount);
}
