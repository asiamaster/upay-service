package com.diligrp.xtrade.upay.trade.service.impl;

import com.diligrp.xtrade.shared.sequence.IKeyGenerator;
import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.service.IAccountChannelService;
import com.diligrp.xtrade.upay.channel.service.IChannelRouteService;
import com.diligrp.xtrade.upay.channel.service.IPaymentChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import com.diligrp.xtrade.upay.core.model.UserAccount;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IFundAccountService;
import com.diligrp.xtrade.upay.core.type.SequenceKey;
import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineRequest;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineResponse;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineTransactionStatus;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;
import com.diligrp.xtrade.upay.trade.domain.ChannelAccount;
import com.diligrp.xtrade.upay.trade.domain.Fee;
import com.diligrp.xtrade.upay.trade.domain.Payment;
import com.diligrp.xtrade.upay.trade.domain.PaymentResult;
import com.diligrp.xtrade.upay.trade.exception.TradePaymentException;
import com.diligrp.xtrade.upay.trade.model.TradeOrder;
import com.diligrp.xtrade.upay.trade.service.IPaymentService;
import com.diligrp.xtrade.upay.trade.service.IPipelineExceptionProcessor;
import com.diligrp.xtrade.upay.trade.service.IPipelinePaymentProcessor;
import com.diligrp.xtrade.upay.trade.type.TradeType;
import com.diligrp.xtrade.upay.trade.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 银行圈提业务
 *
 * @author: brenthuang
 * @date: 2020/12/11
 */
@Service("bankWithdrawPaymentService")
public class BankWithdrawPaymentServiceImpl implements IPaymentService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource
    private IAccountChannelService accountChannelService;

    @Resource
    private IChannelRouteService channelRouteService;

    @Resource
    private IPaymentChannelService paymentChannelService;

    @Resource
    private IFundAccountService fundAccountService;

    @Resource
    private IPipelinePaymentProcessor pipelinePaymentProcessor;

    @Resource
    private IPipelineExceptionProcessor pipelineExceptionProcessor;

    @Resource
    private IAccessPermitService accessPermitService;

    @Resource
    private SnowflakeKeyManager snowflakeKeyManager;


    /**
     * {@inheritDoc}
     *
     * 只支持网银渠道，且在商户收益账户中"退回"手续费给客户，而非收取费用; 专为寿光市场处理网银提现发生异常时使用。
     */
    @Override
    @Transactional(propagation = Propagation.NEVER, rollbackFor = Exception.class)
    public PaymentResult commit(TradeOrder trade, Payment payment) {
        paymentChannelService.supportedChannels(trade.getMchId()).stream()
            .filter(channelType -> channelType.equalTo(payment.getChannelId())).findAny()
            .orElseThrow(() -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "此商户不支持该渠道进行银行圈提"));
        if (!ObjectUtils.equals(trade.getAccountId(), payment.getAccountId())) {
            throw new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "圈提资金账号不一致");
        }
        Optional<List<Fee>> feesOpt = payment.getObjects(Fee.class.getName());
        feesOpt.ifPresent(fees -> { throw new TradePaymentException(ErrorCode.OPERATION_NOT_ALLOWED, "银行圈提不支持自定义费用"); });
        ChannelAccount channelAccount = checkChannelAccount(payment);

        // 检查交易权限, 并选择支付通道
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(trade.getMchId());
        int maxPwdErrors = merchant.configuration().maxPwdErrors();
        UserAccount account = accountChannelService.checkTradePermission(payment.getAccountId(), payment.getPassword(), maxPwdErrors);
        accountChannelService.checkAccountTradeState(account); // 寿光专用业务逻辑

        FundAccount fund = fundAccountService.findFundAccountById(payment.getAccountId());
        if (fund.getBalance() - fund.getFrozenAmount() < payment.getAmount()) {
            throw new TradePaymentException(ErrorCode.INSUFFICIENT_ACCOUNT_FUND, "账户可用余额不足");
        }
        long mchId = merchant.getParentId() == 0 ? merchant.getMchId() : merchant.getParentId();
        IPipeline pipeline = channelRouteService.selectPaymentPipeline(mchId, payment.getChannelId(), trade.getAmount());
        // 生成"处理中"的支付记录
        IKeyGenerator keyGenerator = snowflakeKeyManager.getKeyGenerator(SequenceKey.PAYMENT_ID);
        String paymentId = String.valueOf(keyGenerator.nextId());
        // 向通道发起支付请求
        LocalDateTime when = LocalDateTime.now().withNano(0);
        LOG.info("Sending {} pipeline payment request for {}", pipeline.code(), paymentId);
        PipelineRequest request = PipelineRequest.of(pipeline, paymentId, channelAccount.getToAccount(),
            channelAccount.getToName(), channelAccount.getToType(), payment.getAmount(), channelAccount.getBankNo(),
            channelAccount.getBankName(), when).attach(trade).attach(payment).attach(account);
        request.put("channelId", payment.getChannelId());
        PipelineResponse response = pipeline.sendTradeRequest(request, pipelinePaymentProcessor);

        PipelineTransactionStatus status = new PipelineTransactionStatus(response.getState().getCode(),
            response.getMessage(), response.getStatus());
        return PaymentResult.of(PaymentResult.CODE_SUCCESS, response.getPaymentId(), status);
    }

    @RabbitHandler
    @RabbitListener(queues = {Constants.PIPELINE_EXCEPTION_QUEUE})
    public void recoverPipelinePayment(Message message) {
        try {
            byte[] packet = message.getBody();
            MessageProperties properties = message.getMessageProperties();
            String charSet = properties != null && properties.getContentEncoding() != null ?
                properties.getContentEncoding() : Constants.CHARSET_UTF8;
            String paymentId = new String(packet, charSet);
            LOG.info("Receiving pipeline exception retry request for {}", paymentId);
            boolean success = pipelineExceptionProcessor.incPipelineTryCount(paymentId, LocalDateTime.now());
            if (!success) {
                LOG.warn("no pipeline payment request found for {}", paymentId);
                return;
            }

            Optional<PipelinePayment> pipelinePaymentOpt = pipelineExceptionProcessor.findPipelinePayment(paymentId);
            pipelinePaymentOpt.ifPresent(pipelinePayment -> {
                // 支付申请已经存在处理结果则直接返回
                if (ProcessState.PROCESSING.equalTo(pipelinePayment.getState())) {
                    IPipeline pipeline = channelRouteService.findPaymentPipeline(pipelinePayment.getCode());
                    LOG.info("Sending {} pipeline query request for {}:{}", pipeline.code(), paymentId,
                        pipelinePayment.getRetryCount());
                    PipelineRequest request = PipelineRequest.of(pipeline, paymentId, pipelinePayment.getToAccount(),
                        pipelinePayment.getToName(), pipelinePayment.getToType(), pipelinePayment.getAmount(),
                        pipelinePayment.getBankNo(), pipelinePayment.getBankName(), pipelinePayment.getCreatedTime());
                    pipeline.sendQueryRequest(request.attach(pipelinePayment), pipelineExceptionProcessor);
                } else {
                    LOG.warn("Ignore pipeline query request for {} because of state", paymentId);
                }
            });
        } catch (Exception ex) {
            LOG.error("Failed to process pipeline exception retry request", ex);
        }
    }

    private ChannelAccount checkChannelAccount(Payment payment) {
        Optional<ChannelAccount> channelAccountOpt = payment.getObject(ChannelAccount.class.getName());
        ChannelAccount channelAccount = channelAccountOpt.orElseThrow(
            () -> new TradePaymentException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "channelAccount missed"));
        AssertUtils.notEmpty(channelAccount.getToAccount(), "channel accountNo missed");
        AssertUtils.notEmpty(channelAccount.getToName(), "channel accountName missed");
        AssertUtils.notNull(channelAccount.getToType(), "channel accountType missed");
        AssertUtils.isTrue(channelAccount.getToType() >= 1 && channelAccount.getToType() <= 2,
            "invalid channel accountType");
        return channelAccount;
    }

    @Override
    public TradeType supportType() {
        return TradeType.BANK_WITHDRAW;
    }
}
