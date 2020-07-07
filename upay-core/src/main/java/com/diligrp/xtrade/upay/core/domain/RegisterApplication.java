package com.diligrp.xtrade.upay.core.domain;

/**
 * 应用注册申请模型
 */
public class RegisterApplication {
    // 应用ID
    private Long appId;
    // 商户ID
    private Long mchId;
    // 应用名称
    private String name;
    // 授权Token
    private String accessToken;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
