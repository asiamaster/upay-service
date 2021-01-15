package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.shared.util.CurrencyUtils;
import com.diligrp.xtrade.shared.util.DateUtils;
import com.diligrp.xtrade.shared.util.JsonUtils;
import com.diligrp.xtrade.shared.util.NumberUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.TransactionStatus;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.util.Constants;
import com.diligrp.xtrade.upay.pipeline.client.SjBankNioClient;
import com.diligrp.xtrade.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;
import com.diligrp.xtrade.upay.pipeline.type.Pipeline;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;
import com.openjava.nio.provider.NioNetworkProvider;
import com.openjava.nio.util.ClassUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 盛京银行通道领域模型
 *
 * @author: brenthuang
 * @date: 2020/12/09
 */
@Pipeline(type = PipelineType.SJB_DIRECT)
public class SjBankPipeline extends AbstractPipeline {
    // 消息模板路径
    private static final String TEMPLATE_PATH = "com/diligrp/xtrade/upay/pipeline/template/SJBank.properties";
    // 交易请求模板KEY
    private static final String KEY_TRADE_REQUEST = "sjb.trade.request";
    // 交易查询请求模板KEY
    private static final String KEY_QUERY_REQUEST = "sjb.trade.query.request";

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    // 通道IP
    private String host;
    // 通道端口
    private Integer port;
    // 参数配置
    private Configuration configuration;

    @Override
    public void configPipeline(PipelineType type, String name, String uri, String param, long mchId) {
        super.configPipeline(type, name, uri, param, mchId);
        String[] hosts = uri.split(Constants.CHAR_COLON, 2);
        if (hosts.length != 2 || !NumberUtils.isNumeric(hosts[1])) {
            throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无效的支付通道配置: " + uri);
        }
        this.host = hosts[0];
        this.port = NumberUtils.str2Int(hosts[1], 0);

        if (ObjectUtils.isEmpty(param)) {
            throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "支付通道未进行参数配置");
        }
        configuration = JsonUtils.fromJsonString(param, Configuration.class);
        if (ObjectUtils.isEmpty(configuration.fromAccount) || ObjectUtils.isEmpty(configuration.fromName)) {
            throw new PaymentPipelineException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "无效支付通道参数配置");
        }
    }

    /**
     * {@inheritDoc}
     *
     * 远程通道发起支付申请
     */
    @Override
    public PipelineResponse sendTradeRequest(PipelineRequest request, Callback callback) {
        SjBankNioClient client = null;
        String xmlRequest = null;
        Integer channelId = request.getInteger("channelId");

        try {
            String xmlTemplate = xmlTemplate(KEY_TRADE_REQUEST);
            Map<String, String> params = new HashMap<>();
            // 业务系统流水号
            params.put("paymentId", request.getPaymentId());
            // 交易日期
            params.put("date", DateUtils.formatDateTime(request.getWhen(), "yyyyMMdd"));
            // 交易时间
            params.put("time", DateUtils.formatDateTime(request.getWhen(), "HHmmss"));
            // 付款账户
            params.put("fromAccount", configuration.fromAccount);
            // 付款账户名称
            params.put("fromName", ObjectUtils.trimToEmpty(configuration.fromName));
            // 收款账户
            params.put("toAccount", request.getToAccount());
            // 收款账户名称
            params.put("toName", ObjectUtils.trimToEmpty(request.getToName()));
            // 交易金额-元
            params.put("amount", CurrencyUtils.cent2TenNoSymbol(request.getAmount()));
            // 收款行联行行号
            params.put("bankNo", ObjectUtils.trimToEmpty(request.getBankNo()));
            // 收款行名称
            params.put("bankName", ObjectUtils.trimToEmpty(request.getBankName()));
            // 是否跨行
            params.put("bankFlag", ObjectUtils.equals(channelId, this.channelId()) ? "0": "1");

            StrSubstitutor engine = new StrSubstitutor(params);
            xmlRequest = engine.replace(xmlTemplate);
            // 建立前置机TCP连接
            client = new SjBankNioClient(host, port, NioNetworkProvider.getInstance());
            callback.connectSuccess(request);
        } catch (IOException iex) {
            LOG.error("SJBank pipeline init failed", iex);
            // 接口默认抛出"支付通道不可用"异常
            callback.connectFailed(request);
        } catch (PaymentServiceException pse) {
            // 未发送请求，提前关闭TCP连接
            SjBankNioClient.closeQuietly(client);
            // 保证我方余额不足等异常信息，能够正常抛出
            throw  pse;
        } catch (Exception ex) {
            // 未发送请求，提前关闭TCP连接
            SjBankNioClient.closeQuietly(client);
            throw new PaymentServiceException("支付系统未知异常，请联系系统管理员", ex);
        }

        // client已经连接成功, 否则callback.connectFailed已经抛出异常
        TransactionStatus status = request.getObject(TransactionStatus.class);
        PipelineResponse response = PipelineResponse.of(ProcessState.PROCESSING, request.getPaymentId(), 0L, status);
        try {
            LOG.info("Sending SJBank pipeline trade request: " + xmlRequest);
            String xmlResponse = client.sendPipelineRequest(xmlRequest);
            LOG.info("Received SJBank pipeline trade response: " + xmlResponse);
            SAXReader reader = new SAXReader();
            Document root = reader.read(new StringReader(xmlResponse));
            // 银行方流水号
            Node node = root.selectSingleNode("/ap/head/serial_no");
            String serialNo = node.getStringValue();
            // 成功标识
            node = root.selectSingleNode("/ap/head/succ_flag");
            String flag = node.getStringValue();
            // 返回码
            node = root.selectSingleNode("/ap/head/ret_code");
            String code = node.getStringValue();
            // 返回信息
            node = root.selectSingleNode("/ap/head/ret_info");
            String message = node.getStringValue();
            response.setSerialNo(serialNo);
            response.setFee(0L);
            response.setMessage(message);
            response.setDescription(String.format("succ_flag: %s, ret_code: %s, ret_info: %s", flag, code, message));
            response.setState(checkProcessState(flag, code));
            callback.pipelineSuccess(request, response);
        } catch (Exception ex) {
            LOG.error("SJBank pipeline process failed", ex);
            response.setState(ProcessState.PROCESSING);
            // 任何异常发起异常处理流程
            callback.pipelineFailed(request);
        }

        return response;
    }

    /**
     * {@inheritDoc}
     *
     * 远程通道第一次失败时异常重试处理流程, 查询交易状态更新本地事务
     */
    @Override
    public PipelineResponse sendQueryRequest(PipelineRequest request, Callback callback) {
        SjBankNioClient client = null;
        String xmlRequest = null;
        PipelinePayment payment = request.getObject(PipelinePayment.class);

        try {
            String xmlTemplate = xmlTemplate(KEY_QUERY_REQUEST);
            LocalDateTime now = LocalDateTime.now();
            Map<String, String> params = new HashMap<>();
            params.put("paymentId", request.getPaymentId());
            params.put("date", DateUtils.formatDateTime(now, "yyyyMMdd"));
            params.put("time", DateUtils.formatDateTime(now, "HHmmss"));
            params.put("tradeDate", DateUtils.formatDateTime(request.getWhen(), "yyyyMMdd"));
            params.put("fromAccount", configuration.fromAccount);
            StrSubstitutor engine = new StrSubstitutor(params);
            xmlRequest = engine.replace(xmlTemplate);
            client = new SjBankNioClient(host, port, NioNetworkProvider.getInstance());
            callback.connectSuccess(request);
        } catch (Exception ex) {
            LOG.error("SJBank query pipeline init failed", ex);
            // 未发送请求，提前关闭TCP连接
            SjBankNioClient.closeQuietly(client);
            // callback.pipelineFailed默认抛出异常，流程不会往下继续执行
            callback.pipelineFailed(request);
        }

        PipelineResponse response = PipelineResponse.of(ProcessState.PROCESSING, request.getPaymentId(), 0L, null);
        try {
            LOG.info("Sending SJBank pipeline query request: " + xmlRequest);
            String xmlResponse = client.sendPipelineRequest(xmlRequest);
            LOG.info("Received SJBank pipeline query response: " + xmlResponse);
            SAXReader reader = new SAXReader();
            Document root = reader.read(new StringReader(xmlResponse));
            Node node = root.selectSingleNode("/ap/head/succ_flag");
            String flag = node.getStringValue();
            node = root.selectSingleNode("/ap/head/ret_code");
            String code = node.getStringValue();
            node = root.selectSingleNode("/ap/head/ret_info");
            String message = node.getStringValue();
            if ("0".equals(flag) && "0000".equals(code)) {
                node = root.selectSingleNode("/ap/body/serial_no");
                String serialNo = node.getStringValue();
                node = root.selectSingleNode("/ap/body/stat");
                String state = node.getStringValue();
                response.setState(checkQueryState(flag, code, state));
                message = String.format("%s, stat: %s", payment.getDescription(), state);
                response.setSerialNo(serialNo);
                response.setFee(0L);
                response.setDescription(message);
                callback.pipelineSuccess(request, response);
            } else {
                LOG.error("SJBank query pipeline process failed: succ_flag: {}, ret_code: {}, ret_info: {}",
                    flag, code, message);
                callback.pipelineFailed(request);
            }
        } catch (Exception ex) {
            LOG.error("SJBank query pipeline process exception", ex);
            callback.pipelineFailed(request);
        }
        return response;
    }

    private String xmlTemplate(String key) throws Exception {
        InputStream is = ClassUtils.getDefaultClassLoader().getResourceAsStream(TEMPLATE_PATH);
        Properties properties = new Properties();
        properties.load(is);
        String xmlTemplate = properties.getProperty(key);
        if (ObjectUtils.isEmpty(xmlTemplate)) {
            throw new PaymentPipelineException(ErrorCode.OBJECT_NOT_FOUND, "盛京银行请求模板不存在");
        }
        return xmlTemplate;
    }

    private ProcessState checkProcessState(String flag, String code) {
        if ("0".equals(flag) && "0000".equals(code)) {
            return ProcessState.SUCCESS;
        } else if ((!"0".equals(flag) && !"1".equals(flag) && !"8".equals(flag)) && !"0000".equals(code)) {
            return ProcessState.FAILED;
        } else {
            return ProcessState.PROCESSING;
        }
    }

    private ProcessState checkQueryState(String flag, String code, String state) {
        if ("9".equals(state)) {
            return ProcessState.SUCCESS;
        } else if ("6".equals(state)) {
            return ProcessState.FAILED;
        } else {
            return ProcessState.PROCESSING;
        }
    }

    private static class Configuration {
        // 转出账号
        private String fromAccount;
        // 转出账号名
        private String fromName;

        public String getFromAccount() {
            return fromAccount;
        }

        public void setFromAccount(String fromAccount) {
            this.fromAccount = fromAccount;
        }

        public String getFromName() {
            return fromName;
        }

        public void setFromName(String fromName) {
            this.fromName = fromName;
        }
    }
}