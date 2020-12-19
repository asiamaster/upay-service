package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.shared.util.JsonUtils;
import com.diligrp.xtrade.shared.util.NumberUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.util.Constants;
import com.diligrp.xtrade.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.xtrade.upay.pipeline.model.PaymentPipeline;
import com.diligrp.xtrade.upay.pipeline.type.Pipeline;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;
import com.diligrp.xtrade.upay.pipeline.type.ProcessState;

/**
 * 盛京银行通道领域模型
 *
 * @author: brenthuang
 * @date: 2020/12/09
 */
@Pipeline(type = PipelineType.SJ_BANK)
public class SjBankPipeline extends AbstractPipeline {
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
        PipelineResponse response = new PipelineResponse();
        callback.connectSuccess(request);
        // 调用第三方支付通道, 假设返回成功
        response.setState(ProcessState.PROCESSING);
        response.setSerialNo("123456");
        response.setFee(100L);
        callback.pipelineSuccess(request, response);
        return response;
    }

    /**
     * {@inheritDoc}
     *
     * 远程通道第一次失败时异常重试处理流程, 查询交易状态更新本地事务
     */
    @Override
    public PipelineResponse sendQueryRequest(PipelineRequest request, Callback callback) {
        PipelineResponse response = new PipelineResponse();
        callback.connectSuccess(request);
        // 调用第三方支付通道, 假设返回成功
        response.setState(ProcessState.SUCCESS);
        response.setSerialNo("123456");
        callback.pipelineSuccess(request, response);
        return response;
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