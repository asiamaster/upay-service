package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.shared.util.CurrencyUtils;
import com.diligrp.xtrade.shared.util.DateUtils;
import com.diligrp.xtrade.shared.util.JsonUtils;
import com.diligrp.xtrade.shared.util.NumberUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.util.Constants;
import com.diligrp.xtrade.upay.pipeline.client.SjBankNioClient;
import com.diligrp.xtrade.upay.pipeline.exception.PaymentPipelineException;
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
@Pipeline(type = PipelineType.SJ_BANK)
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
    public void configPipeline(String code, String name, String uri, String param) {
        super.configPipeline(code, name, uri, param);
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

        try {
            String xmlTemplate = xmlTemplate(KEY_TRADE_REQUEST);
            Map<String, String> params = new HashMap<>();
            params.put("paymentId", request.getPaymentId());
            params.put("date", DateUtils.formatDateTime(request.getWhen(), "yyyyMMdd"));
            params.put("time", DateUtils.formatDateTime(request.getWhen(), "HHmmss"));
            params.put("fromAccount", configuration.fromAccount);
            params.put("toAccount", request.getToAccount());
            params.put("amount", CurrencyUtils.cent2TenNoSymbol(request.getAmount()));
            StrSubstitutor engine = new StrSubstitutor(params);
            xmlRequest = engine.replace(xmlTemplate);
            client = new SjBankNioClient(host, port, NioNetworkProvider.getInstance());
            callback.connectSuccess(request);
        } catch (PaymentServiceException pse) {
            throw pse;
        } catch (Exception ex) {
            LOG.error("SJBank pipeline init failed", ex);
            // 接口默认抛出"支付通道不可用"异常
            callback.connectFailed(request);
        }

        // client已经连接成功, 否则callback.connectFailed已经抛出异常
        PipelineResponse response = PipelineResponse.of(ProcessState.PROCESSING, request.getPaymentId(), 0L, null);
        try {
            String xmlResponse = client.sendPipelineRequest(xmlRequest);
            SAXReader reader = new SAXReader();
            Document root = reader.read(new StringReader(xmlResponse));
            Node node = root.selectSingleNode("/ap/head/serial_no");
            String serialNo = node.getStringValue();
            node = root.selectSingleNode("/ap/head/succ_flag");
            String flag = node.getStringValue();
            node = root.selectSingleNode("/ap/head/ret_code");
            String code = node.getStringValue();
            node = root.selectSingleNode("/ap/head/ret_info");
            String message = node.getStringValue();
            response.setSerialNo(serialNo);
            response.setFee(0L);
            response.setDescription(message);
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

        try {
            String xmlTemplate = xmlTemplate(KEY_QUERY_REQUEST);
            LocalDateTime now = LocalDateTime.now();
            Map<String, String> params = new HashMap<>();
            params.put("paymentId", request.getPaymentId());
            params.put("date", DateUtils.formatDateTime(now, "yyyyMMdd"));
            params.put("time", DateUtils.formatDateTime(now, "HHmmss"));
            params.put("tradeDate", DateUtils.formatDateTime(request.getWhen(), "yyyyMMdd"));
            params.put("toAccount", request.getToAccount());
            StrSubstitutor engine = new StrSubstitutor(params);
            xmlRequest = engine.replace(xmlTemplate);
            client = new SjBankNioClient(host, port, NioNetworkProvider.getInstance());
            callback.connectSuccess(request);
        } catch (Exception ex) {
            LOG.error("SJBank query pipeline init failed", ex);
            callback.pipelineFailed(request);
        }

        PipelineResponse response = new PipelineResponse();
        try {
            String xmlResponse = client.sendPipelineRequest(xmlRequest);
            SAXReader reader = new SAXReader();
            Document root = reader.read(new StringReader(xmlResponse));
            Node node = root.selectSingleNode("/ap/head/succ_flag");
            String flag = node.getStringValue();
            node = root.selectSingleNode("/ap/head/ret_code");
            String code = node.getStringValue();
            node = root.selectSingleNode("/ap/body/serial_no");
            String serialNo = node.getStringValue();
            node = root.selectSingleNode("/ap/body/stat");
            String state = node.getStringValue();
            node = root.selectSingleNode("/ap/body/error_info");
            String message = node.getStringValue();

            response.setSerialNo(serialNo);
            response.setFee(0L);
            response.setDescription(message);
            response.setState(checkQueryState(flag, code, state));
            callback.pipelineSuccess(request, response);
        } catch (Exception ex) {
            LOG.error("SJBank query pipeline process failed", ex);
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