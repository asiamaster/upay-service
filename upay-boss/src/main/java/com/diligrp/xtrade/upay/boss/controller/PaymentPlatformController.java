package com.diligrp.xtrade.upay.boss.controller;

import com.diligrp.xtrade.shared.domain.Message;
import com.diligrp.xtrade.shared.domain.MessageEnvelop;
import com.diligrp.xtrade.shared.domain.RequestContext;
import com.diligrp.xtrade.shared.exception.MessageEnvelopException;
import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.sapi.ICallableServiceManager;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.shared.util.JsonUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.boss.util.Constants;
import com.diligrp.xtrade.upay.boss.util.HttpUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 支付服务控制器
 */
@RestController
@RequestMapping("/payment/api")
public class PaymentPlatformController {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICallableServiceManager callableServiceManager;

    @Resource
    private IAccessPermitService accessPermitService;

    @RequestMapping(value = "/gateway.do")
    public void gateway(HttpServletRequest request, HttpServletResponse response) {
        Message<?> result = null;
        ApplicationPermit application = null;
        boolean signCheck = false;

        try {
            String payload = HttpUtils.httpBody(request);
            LOG.debug("payment request received, http body: {}", payload);

            RequestContext context = HttpUtils.requestContext(request);
            String service = context.getString(Constants.PARAM_SERVICE);
            Long appId = context.getLong(Constants.PARAM_APPID);
            String accessToken = context.getString(Constants.PARAM_ACCESS_TOKEN);
            String signature = context.getString(Constants.PARAM_SIGNATURE);
            String charset = context.getString(Constants.PARAM_CHARSET);
            signCheck = ObjectUtils.isNotEmpty(signature);

            AssertUtils.notNull(appId, "appId missed");
            AssertUtils.notEmpty(service, "service missed");
            AssertUtils.notEmpty(payload, "payment request payload missed");

            MessageEnvelop envelop = MessageEnvelop.of(appId, service, accessToken, payload, signature, charset);
            application = checkAccessPermission(context, envelop);
            // 开发阶段: 提供签名信息才进行数据验签
            if (signCheck) {
                envelop.unpackEnvelop(application.getPublicKey());
            }
            result = callableServiceManager.callService(context, envelop);
        } catch (IllegalArgumentException iex) {
            LOG.error(iex.getMessage());
            result = Message.failure(ErrorCode.ILLEGAL_ARGUMENT_ERROR, iex.getMessage());
        } catch (ServiceAccessException sex) {
            LOG.error("Payment service not available exception", sex);
            result = Message.failure(ErrorCode.SERVICE_NOT_AVAILABLE, sex.getMessage());
        } catch (MessageEnvelopException mex) {
            LOG.error("Payment service data verify exception", mex);
            result = Message.failure(ErrorCode.UNAUTHORIZED_ACCESS_ERROR, mex.getMessage());
        } catch (PaymentServiceException pex) {
            LOG.error("Payment service process exception", pex);
            result = Message.failure(pex.getCode(), pex.getMessage());
        } catch (Throwable ex) {
            LOG.error("Payment service unknown exception", ex);
            result = Message.failure(ErrorCode.SYSTEM_UNKNOWN_ERROR, ex.getMessage());
        }

        // 处理数据签名: 忽略签名失败，签名失败时调用方会验签失败
        MessageEnvelop reply = MessageEnvelop.of(null, JsonUtils.toJsonString(result));
        try { // 开发阶段: 调用方提供签名信息才进行返回数据签名
            if (signCheck && application != null) {
                reply.packEnvelop(application.getMerchant().getPrivateKey());
                response.addHeader(Constants.PARAM_SIGNATURE, reply.getSignature());
            }
        } catch (Exception ex) {
            LOG.error("Payment service data sign exception", ex.getMessage());
        }
        HttpUtils.sendResponse(response, reply.getPayload());
    }

    private ApplicationPermit checkAccessPermission(RequestContext context, MessageEnvelop envelop) {
        ApplicationPermit application = accessPermitService.loadApplicationPermit(envelop.getAppId());

        // 校验应用访问权限, 暂时不校验商户状态
        if (!ObjectUtils.equals(envelop.getAccessToken(), application.getAccessToken())) {
            throw new ServiceAccessException(ErrorCode.UNAUTHORIZED_ACCESS_ERROR, "未授权的服务访问");
        }
        context.put(ApplicationPermit.class.getName(), application);
        return application;
    }
}