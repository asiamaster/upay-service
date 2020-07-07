package com.diligrp.xtrade.upay.boss.controller;

import com.diligrp.xtrade.shared.domain.Message;
import com.diligrp.xtrade.shared.domain.RequestContext;
import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.sapi.ICallableServiceManager;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.boss.util.Constants;
import com.diligrp.xtrade.upay.boss.util.HttpUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 支付管理后台控制器
 */
@RestController
@RequestMapping("/payment/spi")
public class BossPlatformController {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICallableServiceManager callableServiceManager;

    @RequestMapping(value = "/boss.do")
    public Message<?> gateway(HttpServletRequest request) {
        try {
            String payload = HttpUtils.httpBody(request);
            LOG.debug("boss request received, http body: {}", payload);
            AssertUtils.notEmpty(payload, "boss request payload missed");

            RequestContext context = HttpUtils.requestContext(request);
            checkAccessPermission(context);
            return callableServiceManager.callService(context, payload);
        } catch (IllegalArgumentException iex) {
            LOG.error(iex.getMessage());
            return Message.failure(ErrorCode.ILLEGAL_ARGUMENT_ERROR, iex.getMessage());
        } catch (ServiceAccessException sex) {
            LOG.error("boss service not available exception", sex);
            return Message.failure(ErrorCode.SERVICE_NOT_AVAILABLE, sex.getMessage());
        } catch (PaymentServiceException pex) {
            LOG.error("boss service process exception", pex);
            return Message.failure(pex.getCode(), pex.getMessage());
        } catch (Throwable ex) {
            LOG.error("boss service unknown exception", ex);
            return Message.failure(ErrorCode.SYSTEM_UNKNOWN_ERROR, ex.getMessage());
        }
    }

    private void checkAccessPermission(RequestContext context) {
        String service = context.getString(Constants.PARAM_SERVICE);
        AssertUtils.notEmpty(service, "service missed");
        if (!service.startsWith(Constants.PARAM_PERMIT_SERVICE)) {
            throw new ServiceAccessException(ErrorCode.UNAUTHORIZED_ACCESS_ERROR, "未授权的服务访问");
        }
    }
}