package com.diligrp.xtrade.upay.core.domain;

/**
 * 商户参数配置接口
 *
 * @author: brenthuang
 * @date: 2021/01/20
 */
public interface IConfiguration {

    /**
     * 最大密码错误次数, -1为无限制次数
     */
    int maxPwdErrors();
}
