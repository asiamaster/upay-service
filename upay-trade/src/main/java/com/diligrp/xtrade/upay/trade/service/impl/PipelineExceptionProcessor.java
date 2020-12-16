package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.KeyGeneratorManager;
import com.diligrp.xtrade.upay.channel.dao.IFrozenOrderDao;
import com.diligrp.xtrade.upay.channel.dao.IUserStatementDao;
import com.diligrp.xtrade.upay.channel.domain.AccountChannel;
import com.diligrp.xtrade.upay.channel.domain.FrozenStateDto;
import com.diligrp.xtrade.upay.channel.domain.IFundTransaction;
import com.diligrp.xtrade.upay.channel.model.FrozenOrder;
import com.diligrp.xtrade.upay.channel.model.UserStatement;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.type.FrozenState;
import com.diligrp.xtrade.upay.channel.type.FrozenType;
import com.diligrp.xtrade.upay.channel.type.StatementType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.AccountType;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.AsyncTaskExecutor;
import com.diligrp.xtrade.upay.pipeline.dao.IPipelinePaymentDao;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineRequest;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineResponse;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePaymentDto;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;
import com.diligrp.xtrade.upay.trade.dao.IPaymentFeeDao;
import com.diligrp.xtrade.upay.trade.dao.ITradeOrderDao;
import com.diligrp.xtrade.upay.trade.dao.ITradePaymentDao;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentStateDto;
import com.diligrp.xtrade.upay.trade.domain.TradeStateDto;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.PaymentFee;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.model.TradePayment;
import com.diligrp.xtrade.upay.trade.service.IPipelineExceptionProcessor;
import com.diligrp.xtrade.upay.trade.type.FundType;
import com.diligrp.xtrade.upay.trade.type.PaymentState;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 通道支付回调处理服务
 *
 * @author: brenthuang
 * @date: 2020/12/15
 */
@Service("pipelineExceptionProcessor")
public class PipelineExceptionProcessor implements IPipelineExceptionProcessor {

    @Resource
    private ITradePaymentDao tradePaymentDao;

    @Resource
    private ITradeOrderDao tradeOrderDao;

    @Resource
    private IPaymentFeeDao paymentFeeDao;

    @Resource
    private IUserStatementDao userStatementDao;

    @Resource
    private IFrozenOrderDao frozenOrderDao;

    @Resource
    private IPipelinePaymentDao pipelinePaymentDao;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * {@inheritDoc}
     *
     * 成功连接远程通道服务时回调
     * 异常处理流程: 当前阶段查询原来的交易单, 为下阶段做数据准备
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void connectSuccess(PipelineRequest request) {
        Optional<PipelinePayment> pipelinePaymentOpt = pipelinePaymentDao.findPipelinePayment(request.getPaymentId());
        PipelinePayment pipelinePayment = pipelinePaymentOpt.orElseThrow(
            () -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "通道支付申请不存在"));
        // 如果通道支付请求不存在, 或通道支付申请已经存在处理结果则直接返回
        if (!ProcessState.PROCESSING.equalTo(pipelinePayment.getState())) {
            throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "通道支付申请已处理完成");
        }

        Optional<TradePayment> paymentOpt = tradePaymentDao.findTradePaymentById(request.getPaymentId());
        TradePayment payment = paymentOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付记录不存在"));
        if (!PaymentState.PROCESSING.equalTo(payment.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "支付申请状态异常");
        }
        Optional<TradeOrder> tradeOpt = tradeOrderDao.findTradeOrderById(payment.getTradeId());
        TradeOrder trade = tradeOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "支付订单不存在"));
        if (!TradeState.FROZEN.equalTo(trade.getState())) {
            throw new TradePaymentException(ErrorCode.INVALID_TRADE_STATE, "支付订单状态异常");
        }
        Optional<FrozenOrder> frozenOrderOpt = frozenOrderDao.findFrozenOrderByPaymentId(request.getPaymentId());
        FrozenOrder frozenOrder = frozenOrderOpt.orElseThrow(() -> new TradePaymentException(ErrorCode.OBJECT_NOT_FOUND, "无资金冻结记录"));
        UserAccount account = fundAccountService.findUserAccountById(payment.getAccountId());

        request.attach(pipelinePayment).attach(account).attach(payment).attach(trade).attach(frozenOrder);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void pipelineSuccess(PipelineRequest request, PipelineResponse response) {
        PipelinePayment pipelinePayment = request.getObject(PipelinePayment.class);
        UserAccount account = request.getObject(UserAccount.class);
        TradePayment payment = request.getObject(TradePayment.class);
        TradeOrder trade = request.getObject(TradeOrder.class);
        FrozenOrder frozenOrder = request.getObject(FrozenOrder.class);

        TransactionStatus status = null;
        LocalDateTime now = LocalDateTime.now().withNano(0);
        // 通道处理成功则解冻并扣减资金
        if (response.getState() == ProcessState.SUCCESS) {
            AccountChannel channel = AccountChannel.of(payment.getPaymentId(), account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            transaction.unfreeze(payment.getAmount());
            transaction.outgo(payment.getAmount(), FundType.FUND.getCode(), FundType.FUND.getName());
            if (response.fee() > 0) {
                transaction.outgo(response.fee(), FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName());
            }
            status = accountChannelService.submit(transaction);
            // 修改冻结订单"已解冻"状态
            FrozenStateDto frozenState = FrozenStateDto.of(frozenOrder.getFrozenId(), FrozenState.UNFROZEN.getCode(),
                frozenOrder.getVersion(), now);
            if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            // 更新支付订单状态
            if (response.fee() > 0) {
                paymentFeeDao.insertPaymentFee(PaymentFee.of(payment.getPaymentId(), response.fee(),
                    FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName(), now));
            }
            PaymentStateDto paymentState = PaymentStateDto.of(payment.getPaymentId(), null, response.getFee(),
                PaymentState.SUCCESS.getCode(), payment.getVersion(), now);
            if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, null,
                TradeState.SUCCESS.getCode(), trade.getVersion(), now);
            if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            // 计算实际操作金额, 生成账户圈提业务账单
            UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(payment.getPaymentId())
                .channelId(payment.getChannelId()).accountId(payment.getAccountId(), account.getParentId())
                .type(StatementType.WITHDRAW.getCode()).typeName(StatementType.WITHDRAW.getName())
                .amount(-payment.getAmount() - response.fee()).fee(response.fee())
                .balance(status.getBalance() + status.getAmount())
                .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount())
                .serialNo(trade.getSerialNo()).state(4).createdTime(now).build();
            userStatementDao.insertUserStatement(statement);

            // 修改通道申请为"处理成功"
            PipelinePaymentDto pipelinePaymentDto = PipelinePaymentDto.of(payment.getPaymentId(), response.getSerialNo(),
                response.getFee(), ProcessState.SUCCESS.getCode(), pipelinePayment.getVersion(), now);
            if (pipelinePaymentDao.compareAndSetState(pipelinePaymentDto) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            // 处理商户收款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
            // 如果是商户账户圈提, 圈提手续费商户自行承担, 不能作为园区收益
            if (response.fee() > 0 && !AccountType.MERCHANT.equalTo(account.getType())) {
                MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
                AccountChannel merChannel = AccountChannel.of(payment.getPaymentId(), merchant.getProfitAccount(), 0L);
                IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
                feeTransaction.income(response.fee(), FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName());
                accountChannelService.submitExclusively(feeTransaction);
            }
        } else if (response.getState() == ProcessState.FAILED) {
            AccountChannel channel = AccountChannel.of(payment.getPaymentId(), account.getAccountId(), account.getParentId());
            IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
            transaction.unfreeze(payment.getAmount());
            status = accountChannelService.submitOnce(transaction);
            // 修改冻结订单"已解冻"状态
            FrozenStateDto frozenState = FrozenStateDto.of(frozenOrder.getFrozenId(), FrozenState.UNFROZEN.getCode(),
                frozenOrder.getVersion(), now);
            if (frozenOrderDao.compareAndSetState(frozenState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, null,
                TradeState.CLOSED.getCode(), trade.getVersion(), now);
            if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            PaymentStateDto paymentState = PaymentStateDto.of(payment.getPaymentId(), null, response.getFee(),
                    PaymentState.FAILED.getCode(), payment.getVersion(), now);
            if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            // 修改通道申请为"处理失败"
            PipelinePaymentDto pipelinePaymentDto = PipelinePaymentDto.of(payment.getPaymentId(),
                ProcessState.FAILED.getCode(), response.getDescription(), pipelinePayment.getVersion(), now);
            if (pipelinePaymentDao.compareAndSetState(pipelinePaymentDto) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
        } else if (response.getState() == ProcessState.PROCESSING) {
            // TODO: 发起异常处理流程
        }
        response.setStatus(status);
    }

    @Override
    public void pipelineFailed(PipelineRequest request) {

    }
}
