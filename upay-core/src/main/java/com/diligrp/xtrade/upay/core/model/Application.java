package com.diligrp.xtrade.upay.core.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

import java.time.LocalDateTime;

/**
 * 接入应用数据模型
 */
public class Application extends BaseDo {
    // 应用ID
    private Long appId;
    // 商户ID
    private Long mchId;
    // 应用名称
    private String name;
    // 授权Token
    private String accessToken;
    // 应用私钥
    private String privateKey;
    // 应用公钥
    private String publicKey;

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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public static Builder builder() {
        return new Application().new Builder();
    }

    public class Builder {
        public Builder appId(Long appId) {
            Application.this.appId = appId;
            return this;
        }

        public Builder mchId(Long mchId) {
            Application.this.mchId = mchId;
            return this;
        }

        public Builder name(String name) {
            Application.this.name = name;
            return this;
        }

        public Builder accessToken(String accessToken) {
            Application.this.accessToken = accessToken;
            return this;
        }

        public Builder privateKey(String privateKey) {
            Application.this.privateKey = privateKey;
            return this;
        }

        public Builder publicKey(String publicKey) {
            Application.this.publicKey = publicKey;
            return this;
        }

        public Builder createdTime(LocalDateTime createdTime) {
            Application.this.createdTime = createdTime;
            return this;
        }

        public Application build() {
            return Application.this;
        }
    }
}
