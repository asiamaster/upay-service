package com.diligrp.xtrade.upay.core.domain;

/**
 * 商户接入许可
 */
public class MerchantPermit {
    // 商户ID
    private Long mchId;
    // 商户编码
    private String code;
    // 收益账户
    private Long profitAccount;
    // 担保账户
    private Long vouchAccount;
    // 押金账户
    private Long pledgeAccount;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public static MerchantPermit of(Long mchId, String code, Long profitAccount, Long vouchAccount, Long pledgeAccount) {
        MerchantPermit permit = new MerchantPermit();
        permit.setMchId(mchId);
        permit.setCode(code);
        permit.setProfitAccount(profitAccount);
        permit.setVouchAccount(vouchAccount);
        permit.setPledgeAccount(pledgeAccount);
        return permit;
    }
}
