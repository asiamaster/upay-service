package com.diligrp.xtrade.upay.core.domain;

/**
 * 商户接入许可
 */
public class MerchantPermit {
    // 商户ID
    private Long mchId;
    // 收益账户
    private Long profitAccount;
    // 担保账户
    private Long vouchAccount;
    // 押金账户
    private Long pledgeAccount;
    // 商户私钥
    private String privateKey;
    // 商户公钥
    private String publicKey;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public Long getProfitAccount() {
        return profitAccount;
    }

    public void setProfitAccount(Long profitAccount) {
        this.profitAccount = profitAccount;
    }

    public Long getVouchAccount() {
        return vouchAccount;
    }

    public void setVouchAccount(Long vouchAccount) {
        this.vouchAccount = vouchAccount;
    }

    public Long getPledgeAccount() {
        return pledgeAccount;
    }

    public void setPledgeAccount(Long pledgeAccount) {
        this.pledgeAccount = pledgeAccount;
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

    public static MerchantPermit of(Long mchId, Long profitAccount, Long vouchAccount, Long pledgeAccount,
                                    String privateKey, String publicKey) {
        MerchantPermit permit = new MerchantPermit();
        permit.setMchId(mchId);
        permit.setProfitAccount(profitAccount);
        permit.setVouchAccount(vouchAccount);
        permit.setPledgeAccount(pledgeAccount);
        permit.setPrivateKey(privateKey);
        permit.setPublicKey(publicKey);
        return permit;
    }
}
