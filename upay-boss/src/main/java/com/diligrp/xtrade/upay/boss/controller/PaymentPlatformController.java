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
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import com.diligrp.xtrade.upay.core.service.IPaymentConfigService;
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

    @Resource
    private IPaymentConfigService paymentConfigService;

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
            Long mchId = context.getLong(Constants.PARAM_MCHID);
            String accessToken = context.getString(Constants.PARAM_ACCESS_TOKEN);
            String signature = context.getString(Constants.PARAM_SIGNATURE);
            String charset = context.getString(Constants.PARAM_CHARSET);

            AssertUtils.notNull(appId, "appId missed");
            AssertUtils.notNull(mchId, "mchId missed");
            AssertUtils.notEmpty(service, "service missed");
            AssertUtils.notEmpty(payload, "payment request payload missed");

            MessageEnvelop envelop = MessageEnvelop.of(appId, service, accessToken, payload, signature, charset);
            application = checkAccessPermission(context, mchId, envelop);
            // 获取"接口数据签名验签"系统配置
            signCheck = paymentConfigService.dataSignSwitch(application.getMerchant().getCode());
            if (signCheck) { // 应用公钥验签
                envelop.unpackEnvelop(application.getAppPublicKey());
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
            result = Message.failure(ErrorCode.SYSTEM_UNKNOWN_ERROR, "系统未知异常，请联系系统管理员");
        }

        // 处理数据签名: 忽略签名失败，签名失败时调用方会验签失败
        MessageEnvelop reply = MessageEnvelop.of(null, JsonUtils.toJsonString(result));
        try {
            if (signCheck) { // 平台私钥签名
                reply.packEnvelop(application.getPrivateKey());
                response.addHeader(Constants.PARAM_SIGNATURE, reply.getSignature());
            }
        } catch (Exception ex) {
            LOG.error("Payment service data sign exception", ex.getMessage());
        }
        HttpUtils.sendResponse(response, reply.getPayload());
    }

    /**
     * 检查接口访问权限，验证应用accessToken
     */
    private ApplicationPermit checkAccessPermission(RequestContext context, Long mchId, MessageEnvelop envelop) {
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(mchId);
        ApplicationPermit application = accessPermitService.loadApplicationPermit(envelop.getAppId());

        // 校验应用访问权限, 暂时不校验商户状态
        if (!ObjectUtils.equals(envelop.getAccessToken(), application.getAccessToken())) {
            throw new ServiceAccessException(ErrorCode.UNAUTHORIZED_ACCESS_ERROR, "未授权的服务访问");
        }
        application.setMerchant(merchant);
        context.put(ApplicationPermit.class.getName(), application);
        return application;
    }
}