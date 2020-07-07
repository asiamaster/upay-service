package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenStateDto;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.service.IFrozenOrderService;
import com.diligrp.xtrade.upay.channel.type.FrozenState;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IFundAccountDao;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 资金冻结/解冻订单服务实现
 */
@Service("frozenOrderService")
public class FrozenOrderServiceImpl implements IFrozenOrderService {

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IFundAccountDao fundAccountDao;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IAccountChannelService accountChannelService;

    /**
     * {@inheritDoc}
     *
     *  人工冻结资金
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public long freeze(FreezeFundDto request) {
        Optional<FrozenType> frozenTypeOpt = FrozenType.getType(request.getType());
        frozenTypeOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持此冻结类型"));
        Optional<FundAccount> accountOpt = fundAccountDao.findFundAccountById(request.getAccountId());
        FundAccount account = accountOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));

        // 冻结资金
        LocalDateTime now = LocalDateTime.now();
        AccountChannel channel = AccountChannel.of(null, request.getAccountId(), request.getBusinessId());
        IFundTransaction transaction = channel.openTransaction(FrozenState.FROZEN.getCode(), now);
        transaction.freeze(request.getAmount());
        accountChannelService.submit(transaction);

        // 创建冻结资金订单
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FROZEN_ID);
        long frozenId = keyGenerator.nextId();
        FrozenOrder frozenOrder = FrozenOrder.builder().frozenId(frozenId).paymentId(null).accountId(request.getAccountId())
            .businessId(request.getBusinessId()).name(account.getName()).type(request.getType()).amount(request.getAmount())
            .state(FrozenState.FROZEN.getCode()).description(request.getDescription()).version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);
        return frozenId;
    }

    /**
     * {@inheritDoc}
     *
     *  人工解冻资金
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void unfreeze(Long frozenId) {
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findFrozenOrderById(frozenId);
        FrozenOrder order = orderOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (order.getState() != FrozenState.FROZEN.getCode()) {
            throw new PaymentChannelException(ErrorCode.OPERATION_NOT_ALLOWED, "无效冻结状态，不能执行解冻操作");
        }
        if (order.getType() == FrozenType.TRADE_FROZEN.getCode()) {
            throw new PaymentChannelException(ErrorCode.OPERATION_NOT_ALLOWED, "不能解冻交易冻结的资金");
        }

        LocalDateTime now = LocalDateTime.now();
        AccountChannel channel = AccountChannel.of(null, order.getAccountId(), order.getBusinessId());
        IFundTransaction transaction = channel.openTransaction(FrozenState.UNFROZEN.getCode(), now);
        transaction.unfreeze(order.getAmount());
        accountChannelService.submit(transaction);

        FrozenStateDto updateState = FrozenStateDto.of(frozenId, FrozenState.UNFROZEN.getCode(),
            order.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(updateState) <= 0) {
            throw new PaymentChannelException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
    }
}
