package com.diligrp.xtrade.upay.core.domain;

import com.diligrp.xtrade.shared.util.JsonUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.util.Constants;
import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * 商户接入许可
 */
public class MerchantPermit {
    // 商户ID
    private Long mchId;
    // 商户编码
    private String code;
    // 商户名称
    private String name;
    // 父商户ID
    private Long parentId;
    // 收益账户
    private Long profitAccount;
    // 担保账户
    private Long vouchAccount;
    // 押金账户
    private Long pledgeAccount;
    // 参数配置
    private IConfiguration configuration;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
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

    public Long parentMchId() {
        return getParentId() == 0 ? getMchId() : getParentId();
    }

    /**
     * 返回商户配置, 如未配置则返回默认配置
     */
    public IConfiguration configuration() {
        return configuration != null ? configuration : Configuration.of(Constants.DEFAULT_MAX_PASSWORD_ERRORS);
    }

    public MerchantPermit config(String params) {
        try {
            if (ObjectUtils.isNotEmpty(params)) {
                configuration = JsonUtils.fromJsonString(params, Configuration.class);
            }
        } catch (Exception ex) {
            throw new PaymentServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "商户配置参数错误");
        }
        return this;
    }

    public static MerchantPermit of(Long mchId, String code, String name, Long parentId, Long profitAccount,
                                    Long vouchAccount, Long pledgeAccount) {
        MerchantPermit permit = new MerchantPermit();
        permit.setMchId(mchId);
        permit.setCode(code);
        permit.setName(name);
        permit.setParentId(parentId);
        permit.setProfitAccount(profitAccount);
        permit.setVouchAccount(vouchAccount);
        permit.setPledgeAccount(pledgeAccount);
        return permit;
    }

    private static class Configuration implements IConfiguration {
        // 最大密码错误次数
        private Integer maxPwdErrors;

        public Integer getMaxPwdErrors() {
            return maxPwdErrors;
        }

        public void setMaxPwdErrors(Integer maxPwdErrors) {
            this.maxPwdErrors = maxPwdErrors;
        }

        public static Configuration of(Integer maxPwdErrors) {
            Configuration configuration = new Configuration();
            configuration.setMaxPwdErrors(maxPwdErrors);
            return configuration;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int maxPwdErrors() {
            return maxPwdErrors == null ? Constants.DEFAULT_MAX_PASSWORD_ERRORS : maxPwdErrors;
        }
    }
}
