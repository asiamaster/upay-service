package com.diligrp.xtrade.upay.core.service.impl;

import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.security.RsaCipher;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IMerchantDao;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.RegisterAccount;
import com.diligrp.xtrade.upay.core.domain.RegisterApplication;
import com.diligrp.xtrade.upay.core.domain.RegisterMerchant;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.model.Application;
import com.diligrp.xtrade.upay.core.model.Merchant;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.AccountType;
import com.diligrp.xtrade.upay.core.type.UseFor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支付平台接入许可服务
 */
@Service("accessPermitService")
public class AccessPermitServiceImpl implements IAccessPermitService {

    @Resource
    private IMerchantDao merchantDao;

    @Resource
    private IFundAccountService fundAccountService;

    private Map<Long, ApplicationPermit> permits = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * 由于商户和应用信息一旦创建基本上不会修改，因此可以缓存在本地JVM中；
     * 如后期需要限制商户状态，则只能缓存在REDIS中，商户状态更新时同步更新缓存
     */
    @Override
    public ApplicationPermit loadApplicationPermit(Long appId) {
        ApplicationPermit permit = permits.get(appId);
        if (permit == null) {
            synchronized (permits) {
                if ((permit = permits.get(appId)) == null) {
                    Optional<Application> application = merchantDao.findApplicationById(appId);
                    permit = application.map(app -> {
                        MerchantPermit merchant = merchantDao.findMerchantById(app.getMchId())
                            .map(mer -> MerchantPermit.of(mer.getMchId(), mer.getProfitAccount(), mer.getVouchAccount(),
                                mer.getPledgeAccount(), mer.getPrivateKey(), mer.getPublicKey()))
                            .orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));
                        return ApplicationPermit.of(app.getAppId(), app.getAccessToken(), app.getPrivateKey(),
                            app.getPublicKey(), merchant);
                    }).orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "应用信息未注册"));
                    permits.put(appId, permit);
                }
            }
        }
        return permit;

        /*Optional<Application> application = merchantDao.findApplicationById(appId);
        return application.map(app -> {
            MerchantPermit merchant = merchantDao.findMerchantById(app.getMchId()).map(mer -> MerchantPermit.of(
                mer.getMchId(), mer.getProfitAccount(), mer.getVouchAccount(), mer.getPledgeAccount(), mer.getPrivateKey(),
                mer.getPublicKey())).orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));
            return ApplicationPermit.of(app.getAppId(), app.getAccessToken(), app.getPrivateKey(), app.getPublicKey(), merchant);
        }).orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "应用信息未注册"));*/
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public MerchantPermit registerMerchant(RegisterMerchant request) {
        Optional<Merchant> merchantOpt = merchantDao.findMerchantById(request.getMchId());
        merchantOpt.ifPresent(merchant -> { throw new PaymentServiceException(ErrorCode.OBJECT_ALREADY_EXISTS, "接入商户已存在"); });

        LocalDateTime now = LocalDateTime.now();
        // 生成收益账号
        RegisterAccount profileAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_PROFIT.getCode()).code(null).name(request.getName()).gender(null).mobile(request.getMobile())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long profileId = fundAccountService.createFundAccount(request.getMchId(), profileAccount);
        // 生成担保账号
        RegisterAccount vouchAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_VOUCH.getCode()).code(null).name(request.getName()).gender(null).mobile(request.getMobile())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long vouchId = fundAccountService.createFundAccount(request.getMchId(), vouchAccount);
        // 生成担保账号
        RegisterAccount pledgeAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_PLEDGE.getCode()).code(null).name(request.getName()).gender(null).mobile(request.getMobile())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long pledgeId = fundAccountService.createFundAccount(request.getMchId(), pledgeAccount);

        String[] keyPair = null;
        try {
            keyPair = RsaCipher.generateRSAKeyPair();
        } catch (Exception ex) {
            throw new PaymentServiceException("生成应用接口安全密钥失败", ex);
        }
        Merchant merchant = Merchant.builder().mchId(request.getMchId()).code(request.getCode()).name(request.getName())
            .profitAccount(profileId).vouchAccount(vouchId).pledgeAccount(pledgeId).address(request.getAddress())
            .contact(request.getContact()).mobile(request.getMobile()).privateKey(keyPair[0]).publicKey(keyPair[1])
            .state(1).createdTime(now).build();
        merchantDao.insertMerchant(merchant);
        return MerchantPermit.of(request.getMchId(), profileId, vouchId, pledgeId, keyPair[0], keyPair[1]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ApplicationPermit registerApplication(RegisterApplication request) {
        Optional<Merchant> merchantOpt = merchantDao.findMerchantById(request.getMchId());
        merchantOpt.orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));
        Optional<Application> applicationOpt = merchantDao.findApplicationById(request.getAppId());
        applicationOpt.ifPresent(application -> new PaymentServiceException(ErrorCode.OBJECT_ALREADY_EXISTS, "接入应用已存在"));

        LocalDateTime now = LocalDateTime.now();
        String[] keyPair;
        try {
            keyPair = RsaCipher.generateRSAKeyPair();
        } catch (Exception ex) {
            throw new PaymentServiceException("生成应用接口安全密钥失败", ex);
        }

        Application application = Application.builder().appId(request.getAppId()).mchId(request.getMchId())
            .name(request.getName()).accessToken(request.getAccessToken()).privateKey(keyPair[0]).publicKey(keyPair[1])
            .createdTime(now).build();
        merchantDao.insertApplication(application);
        return ApplicationPermit.of(request.getAppId(), request.getAccessToken(), keyPair[0], keyPair[1], null);
    }
}
