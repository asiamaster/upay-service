package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.FreezeFundDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenAmount;
import com.diligrp.xtrade.upay.channel.domain.FrozenOrderQuery;
import com.diligrp.xtrade.upay.channel.domain.FrozenStateDto;
import com.diligrp.xtrade.upay.channel.domain.FrozenStatus;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.service.IFrozenOrderService;
import com.diligrp.xtrade.upay.channel.type.FrozenState;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IUserAccountDao;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.exception.FundAccountException;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.AccountStateMachine;
import com.diligrp.xtrade.upay.core.util.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 资金冻结/解冻订单服务实现
 */
@Service("frozenOrderService")
public class FrozenOrderServiceImpl implements IFrozenOrderService {

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IUserAccountDao userAccountDao;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IFundAccountService fundAccountService;

    /**
     * {@inheritDoc}
     *
     *  人工冻结资金
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public FrozenStatus freeze(FreezeFundDto request) {
        Optional<FrozenType> frozenTypeOpt = FrozenType.getType(request.getType());
        frozenTypeOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "不支持此冻结类型"));
        Optional<UserAccount> accountOpt = userAccountDao.findUserAccountById(request.getAccountId());
        UserAccount account = accountOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::frozenFundCheck);

        // 冻结资金
        LocalDateTime now = LocalDateTime.now();
        AccountChannel channel = AccountChannel.of(null, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(FrozenState.FROZEN.getCode(), now);
        transaction.freeze(request.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);

        // 创建冻结资金订单
        IKeyGenerator keyGenerator = keyGeneratorManager.getKeyGenerator(SequenceKey.FROZEN_ID);
        // 异步执行避免Seata回滚造成ID重复
        long frozenId = AsyncTaskExecutor.submit(() -> keyGenerator.nextId());
        FrozenOrder frozenOrder = FrozenOrder.builder().frozenId(frozenId).paymentId(null).accountId(request.getAccountId())
            .name(account.getName()).type(request.getType()).amount(request.getAmount())
            .extension(request.getExtension()).state(FrozenState.FROZEN.getCode()).description(request.getDescription())
            .version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);
        return FrozenStatus.of(frozenId, status);
    }

    /**
     * {@inheritDoc}
     *
     *  人工解冻资金
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public FrozenStatus unfreeze(Long frozenId) {
        Optional<FrozenOrder> orderOpt = frozenOrderDao.findFrozenOrderById(frozenId);
        FrozenOrder order = orderOpt.orElseThrow(() -> new PaymentChannelException(ErrorCode.OBJECT_NOT_FOUND, "冻结订单不存在"));
        if (order.getState() != FrozenState.FROZEN.getCode()) {
            throw new PaymentChannelException(ErrorCode.OPERATION_NOT_ALLOWED, "无效冻结状态，不能执行解冻操作");
        }
        if (order.getType() == FrozenType.TRADE_FROZEN.getCode()) {
            throw new PaymentChannelException(ErrorCode.OPERATION_NOT_ALLOWED, "不能解冻交易冻结的资金");
        }
        Optional<UserAccount> accountOpt = userAccountDao.findUserAccountById(order.getAccountId());
        UserAccount account = accountOpt.orElseThrow(() -> new FundAccountException(ErrorCode.ACCOUNT_NOT_FOUND, "资金账号不存在"));
        accountOpt.ifPresent(AccountStateMachine::frozenFundCheck);

        LocalDateTime now = LocalDateTime.now();
        AccountChannel channel = AccountChannel.of(null, account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(FrozenState.UNFROZEN.getCode(), now);
        transaction.unfreeze(order.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);

        FrozenStateDto updateState = FrozenStateDto.of(frozenId, FrozenState.UNFROZEN.getCode(),
            order.getVersion(), now);
        if (frozenOrderDao.compareAndSetState(updateState) <= 0) {
            throw new PaymentChannelException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
        }
        return FrozenStatus.of(frozenId, status);
    }

    /**
     * {@inheritDoc}
     *
     *  根据主资金账号进行分页查询冻结资金订单，当传入子账号进行查询时，将返回空结果
     */
    @Override
    public PageMessage<FrozenOrder> listFrozenOrders(FrozenOrderQuery query) {
        // 检查资金账号是否存在
        fundAccountService.findUserAccountById(query.getAccountId());
        List<FrozenOrder> frozenOrders = Collections.emptyList();
        long total = frozenOrderDao.countFrozenOrders(query);
        if (total > 0) {
            frozenOrders = frozenOrderDao.listFrozenOrders(query);
        }
        return PageMessage.success(total, frozenOrders);
    }

    /**
     * {@inheritDoc}
     *
     * 参数传入主资金账号ID
     */
    @Override
    public Optional<FrozenAmount> findFrozenAmount(Long accountId) {
        return frozenOrderDao.findFrozenAmount(accountId);
    }
}
