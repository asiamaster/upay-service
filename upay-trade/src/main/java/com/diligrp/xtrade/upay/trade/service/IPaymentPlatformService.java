package com.diligrp.xtrade.upay.trade.service;

import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.trade.domain.ConfirmRequest;
import com.diligrp.xtrade.upay.trade.domain.PaymentRequest;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.domain.RefundRequest;
import com.diligrp.xtrade.upay.trade.domain.TradeRequest;

/**
 * 支付平台服务接口类
 */
public interface IPaymentPlatformService {
    /**
     * 创建交易订单：适用于所有交易类型
     *
     * @param application - 应用接入许可
     * @param trade - 交易请求
     * @return 交易ID
     */
    String createTrade(ApplicationPermit application, TradeRequest trade);

    /**
     * 提交交易支付：适用于所有交易，不同的交易类型有不同的业务处理逻辑
     *
     * @param application - 应用接入许可
     * @param request - 支付请求
     * @return 支付结果
     */
    PaymentResult commit(ApplicationPermit application, PaymentRequest request);

    /**
     * 确认预授权消费：适用于预授权业务（预授权缴费和预授权交易）
     *
     * @param application - 应用接入许可
     * @param request - 确认预授权申请
     * @return 支付结果
     */
    PaymentResult confirm(ApplicationPermit application, ConfirmRequest request);

    /**
     * 撤销交易：撤销预授权业务时将解冻冻结资金，撤销普通业务时将进行资金逆向操作
     *
     * @param application - 应用接入许可
     * @param request - 撤销交易申请
     * @return 支付结果
     */
    PaymentResult cancel(ApplicationPermit application, RefundRequest request);
}
