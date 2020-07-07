package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.RegisterApplication;
import com.diligrp.xtrade.upay.core.domain.RegisterMerchant;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;

import javax.annotation.Resource;

/**
 * 接入许可注册服务组件
 *
 * 接入支付平台需注册商户并在商户下完成注册应用，后续所有接口访问需提供注册应用信息，包括appId和token
 */
@CallableComponent(id = "payment.permit.register")
public class AccessPermitComponent {
    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * 注册商户: 创建接入商户(分配mchId)、创建商户账户（收益账户、担保账户和押金账户等）并分配商户公私钥
     */
    public MerchantPermit merchant(ServiceRequest<RegisterMerchant> request) {
        RegisterMerchant merchant = request.getData();
        AssertUtils.notNull(merchant.getMchId(), "mchId missed");
        AssertUtils.notEmpty(merchant.getCode(), "code missed");
        AssertUtils.notEmpty(merchant.getName(), "name missed");
        AssertUtils.notEmpty(merchant.getPassword(), "password missed");
        return accessPermitService.registerMerchant(merchant);
    }

    /**
     * 注册应用: 创建商户应用(分配appId和accessToken)，分配商户应用公私钥
     */
    public ApplicationPermit application(ServiceRequest<RegisterApplication> request) {
        RegisterApplication application = request.getData();
        AssertUtils.notNull(application.getAppId(), "appId missed");
        AssertUtils.notNull(application.getMchId(), "mchId missed");
        AssertUtils.notEmpty(application.getName(), "name missed");
        AssertUtils.notEmpty(application.getAccessToken(), "accessToken missed");

        return accessPermitService.registerApplication(application);
    }
}
