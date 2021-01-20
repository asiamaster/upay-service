package com.diligrp.xtrade.upay.core.service.impl;

import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.security.RsaCipher;
import com.diligrp.xtrade.shared.util.RandomUtils;
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

    private Map<Long, ApplicationPermit> applications = new ConcurrentHashMap<>();

    private Map<Long, MerchantPermit> merchants = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * 由于商户一旦创建基本上不会修改，因此可以缓存在本地JVM中；
     * 如后期需要限制商户状态，则只能缓存在REDIS中，商户状态更新时同步更新缓存
     */
    @Override
    public MerchantPermit loadMerchantPermit(Long mchId) {
        MerchantPermit permit = merchants.get(mchId);
        if (permit == null) {
            synchronized (merchants) {
                if ((permit = merchants.get(mchId)) == null) {
                    permit = merchantDao.findMerchantById(mchId)
                        .map(mer -> MerchantPermit.of(mer.getMchId(), mer.getCode(), mer.getName(), mer.getParentId(),
                            mer.getProfitAccount(), mer.getVouchAccount(), mer.getPledgeAccount()).config(mer.getParam()))
                        .orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "商户信息未注册"));
                    merchants.put(mchId, permit);
                }
            }
        }
        return permit;
    }

    /**
     * {@inheritDoc}
     *
     * 由于应用信息一旦创建基本上不会修改，因此可以缓存在本地JVM中；
     */
    @Override
    public ApplicationPermit loadApplicationPermit(Long appId) {
        ApplicationPermit permit = applications.get(appId);
        if (permit == null) {
            synchronized (applications) {
                if ((permit = applications.get(appId)) == null) {
                    permit = merchantDao.findApplicationById(appId)
                        .map(app -> ApplicationPermit.of(app.getAppId(), app.getAccessToken(), app.getAppPrivateKey(),
                            app.getAppPublicKey(), app.getPrivateKey(), app.getPublicKey()))
                        .orElseThrow(() -> new ServiceAccessException(ErrorCode.OBJECT_NOT_FOUND, "应用信息未注册"));
                    applications.put(appId, permit);
                }
            }
        }

        return permit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public MerchantPermit registerMerchant(RegisterMerchant request) {
        Optional<Merchant> merchantOpt = merchantDao.findMerchantById(request.getMchId());
        merchantOpt.ifPresent(merchant -> { throw new PaymentServiceException(ErrorCode.OBJECT_ALREADY_EXISTS, "接入商户已存在");});
        request.ifParentId(parentId -> {
            Optional<Merchant> parentOpt = merchantDao.findMerchantById(parentId);
            Merchant parent = parentOpt.orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "父商户不存在"));
            if (parent.getParentId() != 0) {
                throw new PaymentServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "不能在子商户下创建商户");
            }
        });

        LocalDateTime now = LocalDateTime.now();
        // 生成收益账号
        RegisterAccount profileAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_PROFIT.getCode()).code(null).name(request.getName()).gender(null).mobile(request.getMobile())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long profileId = fundAccountService.createUserAccount(request.getMchId(), profileAccount);
        // 生成担保账号
        RegisterAccount vouchAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_VOUCH.getCode()).code(null).name(request.getName()).gender(null).mobile(request.getMobile())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long vouchId = fundAccountService.createUserAccount(request.getMchId(), vouchAccount);
        // 生成担保账号
        RegisterAccount pledgeAccount = RegisterAccount.builder().customerId(0L).type(AccountType.MERCHANT.getCode())
            .useFor(UseFor.FOR_PLEDGE.getCode()).code(null).name(request.getName()).gender(null).mobile(request.getMobile())
            .email(null).idCode(null).address(request.getAddress()).password(request.getPassword()).build();
        long pledgeId = fundAccountService.createUserAccount(request.getMchId(), pledgeAccount);

        Merchant merchant = Merchant.builder().mchId(request.getMchId()).code(request.getCode()).name(request.getName())
            .parentId(0L).profitAccount(profileId).vouchAccount(vouchId).pledgeAccount(pledgeId).address(request.getAddress())
            .contact(request.getContact()).mobile(request.getMobile()).state(1).createdTime(now).build();
        request.ifParentId(parentId -> merchant.setParentId(parentId));
        merchantDao.insertMerchant(merchant);
        return MerchantPermit.of(request.getMchId(), request.getCode(), request.getName(), merchant.getParentId(),
            profileId, vouchId, pledgeId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyMerchant(RegisterMerchant request) {
        Optional<Merchant> merchantOpt = merchantDao.findMerchantById(request.getMchId());
        merchantOpt.orElseThrow(() -> new PaymentServiceException(ErrorCode.OBJECT_NOT_FOUND, "接入商户不存在") );

        LocalDateTime now = LocalDateTime.now();
        Merchant merchant = Merchant.builder().mchId(request.getMchId()).code(request.getCode()).name(request.getName())
            .address(request.getAddress()).contact(request.getContact()).mobile(request.getMobile()).modifiedTime(now).build();
        merchantDao.updateMerchant(merchant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public ApplicationPermit registerApplication(RegisterApplication request) {
        Optional<Application> applicationOpt = merchantDao.findApplicationById(request.getAppId());
        applicationOpt.ifPresent(app -> { throw new PaymentServiceException(ErrorCode.OBJECT_ALREADY_EXISTS, "接入应用已存在");});

        LocalDateTime now = LocalDateTime.now();
        ApplicationPermit permit;
        try {
            String[] appKeyPair = RsaCipher.generateRSAKeyPair(), keyPair = RsaCipher.generateRSAKeyPair();
            permit = ApplicationPermit.of(request.getAppId(), RandomUtils.randomString(8),
                appKeyPair[0], appKeyPair[1], keyPair[0], keyPair[1]);
        } catch (Exception ex) {
            throw new PaymentServiceException("生成应用安全密钥失败", ex);
        }
        Application application = Application.builder().appId(request.getAppId()).mchId(0L)
            .name(request.getName()).accessToken(permit.getAccessToken()).appPrivateKey(permit.getAppPrivateKey())
            .appPublicKey(permit.getAppPublicKey()).privateKey(permit.getPrivateKey()).publicKey(permit.getPublicKey())
            .createdTime(now).build();
        merchantDao.insertApplication(application);
        return permit;
    }
}
