package com.diligrp.xtrade.upay.sentinel.service;

import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.sentinel.domain.RiskControlContext;
import com.diligrp.xtrade.upay.sentinel.domain.RiskControlEngine;

/**
 * 风险控制服务接口
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public interface IRiskControlService {
    /**
     * 加载资金账号风控引擎
     */
    RiskControlEngine loadRiskControlEngine(UserAccount account);

    /**
     * 获取商户全局风控信息
     */
    RiskControlContext findGlobalRiskControl(Long mchId);

    /**
     * 更新商户全局风控信息
     */
    void updateGlobalRiskControl(Long mchId, RiskControlContext context);

    /**
     * 获取用户账号的风控信息
     */
    RiskControlContext findUserRiskControl(UserAccount userAccount);

    /**
     * 更新账户风控信息
     */
    void updateUserRiskControl(UserAccount account, RiskControlContext context);
}
