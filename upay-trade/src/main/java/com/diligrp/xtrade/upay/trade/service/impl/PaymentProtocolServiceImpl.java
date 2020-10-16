package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.service.IPaymentConfigService;
import com.diligrp.xtrade.upay.core.type.AccountState;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.trade.dao.IUserProtocolDao;
import com.diligrp.xtrade.upay.trade.domain.ProtocolQuery;
import com.diligrp.xtrade.upay.trade.domain.ProtocolRegister;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.exception.UserProtocolException;
import com.diligrp.xtrade.upay.trade.model.UserProtocol;
import com.diligrp.xtrade.upay.trade.service.IPaymentProtocolService;
import com.diligrp.xtrade.upay.trade.type.ProtocolState;
import com.diligrp.xtrade.upay.trade.type.ProtocolType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 免密支付协议服务实现
 *
 * @author: brenthuang
 * @date: 2020/10/12
 */
@Service("paymentProtocolService")
public class PaymentProtocolServiceImpl implements IPaymentProtocolService {

    @Resource
    private IUserProtocolDao userProtocolDao;

    @Resource
    private IPaymentConfigService paymentConfigService;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public UserProtocol registerUserProtocol(ProtocolRegister request) {
        ProtocolType.getType(request.getType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的免密协议类型"));
        userProtocolDao.findUserProtocol(request.getAccountId(), request.getType())
            .ifPresent(proto -> {throw new TradePaymentException(ErrorCode.OBJECT_ALREADY_EXISTS, "免密协议已存在");});
        UserAccount account = accountChannelService.checkTradePermission(request.getAccountId(), request.getPassword(), -1);
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(account.getMchId());
        Long maxAmount = paymentConfigService.maxProtocolAmount(merchant.getCode());
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PROTOCOL_ID);

        LocalDateTime now = LocalDateTime.now();
        UserProtocol protocol = UserProtocol.builder().protocolId(keyGenerator.nextId()).accountId(request.getAccountId())
                .name(account.getName()).type(request.getType()).minAmount(0L).maxAmount(maxAmount).startOn(now)
                .state(ProtocolState.NORMAL.getCode()).version(0).createdTime(now).build();
        userProtocolDao.insertUserProtocol(protocol);
        return protocol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserProtocol queryUserProtocol(ProtocolQuery request) {
        ProtocolType.getType(request.getType()).orElseThrow(() ->
            new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持的免密协议类型"));
        UserAccount account = fundAccountService.findUserAccountById(request.getAccountId());
        Optional<UserProtocol> protocolOpt = userProtocolDao.findUserProtocol(request.getAccountId(), request.getType());
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(account.getMchId());
        Long maxAmount = paymentConfigService.maxProtocolAmount(merchant.getCode());
        protocolOpt.ifPresentOrElse(protocol -> {
            if (protocol.getState() != ProtocolState.NORMAL.getCode()) {
                throw new UserProtocolException(UserProtocolException.USE_NOT_ALLOWED, "不允许使用免密支付，协议已被禁用");
            }
            if (request.getAmount() > maxAmount) {
                throw new UserProtocolException(UserProtocolException.USE_NOT_ALLOWED, "不允许使用免密支付，支付金额超出协议金额范围");
            }
        }, () -> {
            if (request.getAmount() <= maxAmount) {
                throw new UserProtocolException(UserProtocolException.OPEN_ALLOWED, "允许开通免密支付");
            } else {
                throw new UserProtocolException(UserProtocolException.OPEN_NOT_ALLOWED, "不允许开通免密支付，支付金额超出协议金额范围");
            }
        });
        return protocolOpt.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserAccount checkProtocolPermission(long accountId, long protocolId, long amount) {
        UserAccount account = fundAccountService.findUserAccountById(accountId);
        if (account.getState() != AccountState.NORMAL.getCode()) {
            throw new PaymentChannelException(ErrorCode.INVALID_ACCOUNT_STATE,
                "资金账户已" + AccountState.getName(account.getState()));
        }

        Optional<UserProtocol> protoOpt = userProtocolDao.findUserProtocolById(protocolId);
        UserProtocol proto = protoOpt.orElseThrow(() -> new UserProtocolException(ErrorCode.OBJECT_NOT_FOUND, "免密支付协议不存在"));
        if (proto.getState() != ProtocolState.NORMAL.getCode()) {
            throw new UserProtocolException(ErrorCode.OPERATION_NOT_ALLOWED, "不允许使用免密支付，协议状态异常");
        }
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(account.getMchId());
        Long maxAmount = paymentConfigService.maxProtocolAmount(merchant.getCode());
        if (amount > maxAmount) {
            throw new UserProtocolException(ErrorCode.OPERATION_NOT_ALLOWED, "不允许使用免密支付，支付金额超出协议金额范围");
        }

        return account;
    }
}
