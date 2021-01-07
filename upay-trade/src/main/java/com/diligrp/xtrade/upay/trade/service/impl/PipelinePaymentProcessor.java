package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.domain.PageMessage;
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
import com.diligrp.xtrade.upay.core.type.AccountType;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.core.util.AsyncTaskExecutor;
import com.diligrp.xtrade.upay.pipeline.dao.IPipelinePaymentDao;
import com.diligrp.xtrade.upay.pipeline.domain.PipelinePaymentDto;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineRequest;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineResponse;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineStatementQuery;
import com.diligrp.xtrade.upay.pipeline.domain.UserPipelineStatement;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;
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
import com.diligrp.xtrade.upay.trade.service.IPipelinePaymentProcessor;
import com.diligrp.xtrade.upay.trade.type.FundType;
import com.diligrp.xtrade.upay.trade.type.PaymentState;
import com.diligrp.xtrade.upay.trade.type.TradeState;
import com.diligrp.xtrade.upay.trade.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 通道支付回调处理服务
 *
 * @author: brenthuang
 * @date: 2020/12/12
 */
@Service("pipelinePaymentProcessor")
public class PipelinePaymentProcessor implements IPipelinePaymentProcessor {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

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
    private IAccountChannelService accountChannelService;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private KeyGeneratorManager keyGeneratorManager;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * {@inheritDoc}
     *
     * 成功连接远程通道服务时回调
     * 执行冻结资金, 生成"处理中"的支付记录, 如果这一阶段本地执行失败则整个业务事务失败
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void connectSuccess(PipelineRequest request) {
        TradeOrder trade = request.getObject(TradeOrder.class);
        Payment payment = request.getObject(Payment.class);
        UserAccount account = request.getObject(UserAccount.class);
        LocalDateTime now = request.getWhen();

        // 发送通道请求前冻结资金
        AccountChannel channel = AccountChannel.of(request.getPaymentId(), account.getAccountId(), account.getParentId());
        IFundTransaction transaction = channel.openTransaction(trade.getType(), now);
        transaction.freeze(payment.getAmount());
        TransactionStatus status = accountChannelService.submit(transaction);
        // 创建冻结资金订单
        Long masterAccountId = account.getParentId() == 0 ? account.getAccountId() : account.getParentId();
        Long childAccountId = account.getParentId() == 0 ? null : account.getAccountId();
        IKeyGenerator frozenKey = keyGeneratorManager.getKeyGenerator(SequenceKey.FROZEN_ID);
        // 异步执行避免Seata回滚造成ID重复
        long frozenId = AsyncTaskExecutor.submit(() -> frozenKey.nextId());
        FrozenOrder frozenOrder = FrozenOrder.builder().frozenId(frozenId).paymentId(request.getPaymentId())
            .accountId(masterAccountId).childId(childAccountId).name(account.getName())
            .type(FrozenType.TRADE_FROZEN.getCode()).amount(payment.getAmount()).state(FrozenState.FROZEN.getCode())
            .description(null).version(0).createdTime(now).build();
        frozenOrderDao.insertFrozenOrder(frozenOrder);
        // 创建支付中的支付记录
        TradePayment paymentDo = TradePayment.builder().paymentId(request.getPaymentId()).tradeId(trade.getTradeId())
            .channelId(payment.getChannelId()).accountId(trade.getAccountId()).name(trade.getName()).cardNo(null)
            .amount(payment.getAmount()).fee(0L).state(PaymentState.PROCESSING.getCode())
            .description(null).version(0).createdTime(now).build();
        tradePaymentDao.insertTradePayment(paymentDo);
        // 冻结交易订单
        TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), TradeState.FROZEN.getCode(), trade.getVersion(), now);
        int result = tradeOrderDao.compareAndSetState(tradeState);
        if (result == 0) {
            throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统正忙，请稍后重试");
        }
        trade.setVersion(trade.getVersion() + 1);
        // 生成"处理中"的通道申请
        PipelinePayment pipelinePayment = PipelinePayment.builder().paymentId(request.getPaymentId()).tradeId(trade.getTradeId())
            .code(request.getPipeline().code()).toAccount(request.getToAccount()).toName(request.getToName())
            .toType(request.getToType()).bankNo(request.getBankNo()).bankName(request.getBankName()).serialNo(null)
            .amount(request.getAmount()).fee(0L).state(ProcessState.PROCESSING.getCode())
            .version(0).retryCount(0).createdTime(now).build();
        pipelinePaymentDao.insertPipelinePayment(pipelinePayment);
        request.attach(paymentDo).attach(frozenOrder).attach(pipelinePayment).attach(status);
    }

    /**
     * {@inheritDoc}
     *
     * 成功调用远程通道服务并获取明确处理结果时回调;
     * 通道返回成功时解冻并扣减账户资金并更改交易状态为"成功", 通道返回失败时解冻资金并更改交易状态为"失败";
     * 通道返回处理中时发起异常处理流程而不执行任何本地操作, 这一阶段本地执行失败时也应该转入异常处理流程
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void pipelineSuccess(PipelineRequest request, PipelineResponse response) {
        TradeOrder trade = request.getObject(TradeOrder.class);
        TradePayment payment = request.getObject(TradePayment.class);
        UserAccount account = request.getObject(UserAccount.class);
        FrozenOrder frozenOrder = request.getObject(FrozenOrder.class);
        PipelinePayment pipelinePayment = request.getObject(PipelinePayment.class);
        TransactionStatus status = request.getObject(TransactionStatus.class);
        String paymentId = payment.getPaymentId();
        LocalDateTime now = LocalDateTime.now().withNano(0);
        LOG.info("{} pipeline payment process result:{}", request.getPipeline().code(), response.getState().name());
        // 通道处理成功则解冻并扣减资金
        if (response.getState() == ProcessState.SUCCESS) {
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
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
                paymentFeeDao.insertPaymentFee(PaymentFee.of(paymentId, response.fee(),
                    FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName(), now));
            }
            PaymentStateDto paymentState = PaymentStateDto.of(paymentId, null, response.getFee(),
                PaymentState.SUCCESS.getCode(), payment.getVersion(), now);
            if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            TradeStateDto tradeState = TradeStateDto.of(trade.getTradeId(), null, null,
                TradeState.SUCCESS.getCode(), trade.getVersion(), now);
            if (tradeOrderDao.compareAndSetState(tradeState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            // 计算实际操作金额, 生成交易冲正时账户业务账单
            UserStatement statement = UserStatement.builder().tradeId(trade.getTradeId()).paymentId(paymentId)
                .channelId(payment.getChannelId()).accountId(payment.getAccountId(), account.getParentId())
                .type(StatementType.WITHDRAW.getCode()).typeName(StatementType.WITHDRAW.getName())
                .amount(-payment.getAmount() - response.fee()).fee(response.fee())
                .balance(status.getBalance() + status.getAmount())
                .frozenAmount(status.getFrozenBalance() + status.getFrozenAmount())
                .serialNo(trade.getSerialNo()).state(4).createdTime(now).build();
            userStatementDao.insertUserStatement(statement);

            // 修改通道申请为"处理成功"
            PipelinePaymentDto pipelinePaymentDto = PipelinePaymentDto.of(paymentId, response.getSerialNo(),
                response.getFee(), ProcessState.SUCCESS.getCode(), pipelinePayment.getVersion(), now);
            if (pipelinePaymentDao.compareAndSetState(pipelinePaymentDto) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }

            // 处理商户收款 - 最后处理园区收益，保证尽快释放共享数据的行锁以提高系统并发
            // 如果是商户账户圈提, 圈提手续费商户自行承担, 不能作为园区收益
            if (response.fee() > 0 && !AccountType.MERCHANT.equalTo(account.getType())) {
                MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
                AccountChannel merChannel = AccountChannel.of(paymentId, merchant.getProfitAccount(), 0L);
                IFundTransaction feeTransaction = merChannel.openTransaction(trade.getType(), now);
                feeTransaction.income(response.fee(), FundType.POUNDAGE.getCode(), FundType.POUNDAGE.getName());
                accountChannelService.submitExclusively(feeTransaction);
            }
        } else if (response.getState() == ProcessState.FAILED) {
            AccountChannel channel = AccountChannel.of(paymentId, account.getAccountId(), account.getParentId());
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
            PaymentStateDto paymentState = PaymentStateDto.of(paymentId, null, response.getFee(),
                PaymentState.FAILED.getCode(), payment.getVersion(), now);
            if (tradePaymentDao.compareAndSetState(paymentState) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
            // 修改通道申请为"处理失败"
            PipelinePaymentDto pipelinePaymentDto = PipelinePaymentDto.of(paymentId,
                ProcessState.FAILED.getCode(), response.getDescription(), pipelinePayment.getVersion(), now);
            if (pipelinePaymentDao.compareAndSetState(pipelinePaymentDto) == 0) {
                throw new TradePaymentException(ErrorCode.DATA_CONCURRENT_UPDATED, "系统忙，请稍后再试");
            }
        } else if (response.getState() == ProcessState.PROCESSING) {
            // 通道接口返回"处理中"则发起第一次异常处理流程
            pipelineFailed(request);
        }
        response.setStatus(status);
    }

    /**
     * {@inheritDoc}
     *
     * 调用远程通道服务失败时(接口超时, 网络异常)回调;
     * 直接执行异常处理流程, 分别间隔1min 5min 10min 15min 120min向远程通道发起查询交易状态申请并返回结果进行处理
     */
    @Override
    public void pipelineFailed(PipelineRequest request) {
        PipelinePayment pipelinePayment = request.getObject(PipelinePayment.class);
        int retryCount = pipelinePayment.getRetryCount();
        if (retryCount < Constants.MAX_EXCEPTION_RETRY_TIMES) {
            try {
                MessageProperties properties = new MessageProperties();
                properties.setContentEncoding(Constants.CHARSET_UTF8);
                properties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
                // 除第一次和最后一次, 延迟处理时间为5*N分钟, N为当前重试次数
                long expiredTime = Constants.MIN_MESSAGE_DELAY_TIME;
                if (retryCount > 0) {
                    expiredTime = retryCount + 1 >= Constants.MAX_EXCEPTION_RETRY_TIMES ?
                        Constants.MAX_MESSAGE_DELAY_TIME : retryCount * Constants.FIVE_MINUTES_IN_MILLIS;
                }
                properties.setExpiration(String.valueOf(expiredTime));
                LOG.info("Making pipeline exception retry request for {}:{}", request.getPaymentId(), retryCount + 1);
                Message message = new Message(request.getPaymentId().getBytes(Constants.CHARSET_UTF8), properties);
                rabbitTemplate.send(Constants.PIPELINE_RECOVER_EXCHANGE, Constants.PIPELINE_RECOVER_KEY, message);
            } catch (Exception ex) {
                LOG.error(String.format("Failed to make pipeline exception retry request for %s:%s",
                    request.getPaymentId(), retryCount + 1), ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageMessage<UserPipelineStatement> listPipelineStatements(PipelineStatementQuery query) {
        long total = pipelinePaymentDao.countPipelineStatements(query);
        List<UserPipelineStatement> statements = Collections.emptyList();
        if (total > 0) {
            statements = pipelinePaymentDao.listPipelineStatements(query);
        }

        return PageMessage.success(total, statements);
    }
}
