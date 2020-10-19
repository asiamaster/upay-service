package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.domain.ProtocolId;
import com.diligrp.xtrade.upay.trade.domain.ProtocolQuery;
import com.diligrp.xtrade.upay.trade.domain.ProtocolRegister;
import com.diligrp.xtrade.upay.trade.model.UserProtocol;
import com.diligrp.xtrade.upay.trade.service.IPaymentProtocolService;

import javax.annotation.Resource;

/**
 * 免密支付服务组件
 *
 * @author: brenthuang
 * @date: 2020/10/12
 */
@CallableComponent(id = "payment.protocol.service")
public class PaymentProtocolComponent {

    @Resource
    private IPaymentProtocolService paymentProtocolService;

    /**
     * 注册免密支付协议
     */
    public ProtocolId register(ServiceRequest<ProtocolRegister> request) {
        ProtocolRegister protocol = request.getData();
        AssertUtils.notNull(protocol.getType(), "type missed");
        AssertUtils.notNull(protocol.getAccountId(), "accountId missed");
        AssertUtils.notEmpty(protocol.getPassword(), "password missed");
        UserProtocol userProtocol = paymentProtocolService.registerUserProtocol(protocol);
        return ProtocolId.of(userProtocol.getProtocolId());
    }

    /**
     * 查询免密支付协议状态
     */
    public ProtocolId query(ServiceRequest<ProtocolQuery> request) {
        ProtocolQuery query = request.getData();
        AssertUtils.notNull(query.getType(), "type missed");
        AssertUtils.notNull(query.getAccountId(), "accountId missed");
        AssertUtils.notNull(query.getAmount(), "amount missed");
        AssertUtils.isTrue(query.getAmount() > 0, "Invalid amount");
        UserProtocol userProtocol = paymentProtocolService.queryUserProtocol(query);
        return ProtocolId.of(userProtocol.getProtocolId());
    }
}
