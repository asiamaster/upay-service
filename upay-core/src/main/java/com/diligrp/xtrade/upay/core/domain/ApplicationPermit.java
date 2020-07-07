package com.diligrp.xtrade.upay.core.domain;

/**
 * 应用接入许可
 */
public class ApplicationPermit {
    // 应用ID
    private Long appId;
    // 授权Token
    private String accessToken;
    // 应用私钥
    private String privateKey;
    // 应用公钥
    private String publicKey;
    // 商户信息
    private MerchantPermit merchant;

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
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

    public MerchantPermit getMerchant() {
        return merchant;
    }

    public void setMerchant(MerchantPermit merchant) {
        this.merchant = merchant;
    }

    public static ApplicationPermit of(Long appId, String accessToken, String privateKey, String publicKey, MerchantPermit merchant) {
        ApplicationPermit permit = new ApplicationPermit();
        permit.setAppId(appId);
        permit.setAccessToken(accessToken);
        permit.setPrivateKey(privateKey);
        permit.setPublicKey(publicKey);
        permit.setMerchant(merchant);

        return permit;
    }
}
