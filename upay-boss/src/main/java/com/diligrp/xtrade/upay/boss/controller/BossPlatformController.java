package com.diligrp.xtrade.upay.boss.controller;

import com.diligrp.xtrade.shared.domain.Message;
import com.diligrp.xtrade.shared.domain.RequestContext;
import com.diligrp.xtrade.shared.exception.ServiceAccessException;
import com.diligrp.xtrade.shared.sapi.ICallableServiceManager;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.shared.util.DateUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.boss.util.Constants;
import com.diligrp.xtrade.upay.boss.util.HttpUtils;
import com.diligrp.xtrade.upay.channel.service.IAccountSnapshotService;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 支付管理后台控制器
 */
@RestController
@RequestMapping("/payment/spi")
public class BossPlatformController {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ICallableServiceManager callableServiceManager;

    @Resource
    private IAccountSnapshotService accountSnapshotService;

    private ExecutorService executorService = new ThreadPoolExecutor(1, 1, 30,
            TimeUnit.SECONDS, new SynchronousQueue<>(), new ThreadPoolExecutor.AbortPolicy());

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

    @RequestMapping(value = "/snapshot.do")
    public Message<?> snapshot(HttpServletRequest request) {
        try {
            // 未指定快照时间时默认快照时间为"昨天"
            RequestContext context = HttpUtils.requestContext(request);
            String dayOn = context.getString("snapshotOn");
            LocalDate snapshotOn = ObjectUtils.isEmpty(dayOn) ?
                LocalDate.now().minusDays(1) : DateUtils.parseDate(dayOn, DateUtils.YYYYMMDD);
            // 快照线程池采用单线程实现(指定Rejected策略为AbortPolicy, 采用SynchronousQueue作为任务队列)；
            // 确保一台虚拟机某一时刻只能运行一个快照任务，由于服务层采用Snapshot Guard限制了同一天只能有一个快照任务，
            // 因此无须使用分布式锁限制全局只能运行一个快照任务
            executorService.execute(() -> {
                accountSnapshotService.makeAccountSnapshot(snapshotOn);
            });

            Message<?> message = Message.success();
            message.setMessage("成功提交快照任务");
            return message;
        } catch (RejectedExecutionException iex) {
            LOG.error("Snapshot task is already running on this server, skip this snapshot task");
            return Message.failure(ErrorCode.OPERATION_NOT_ALLOWED, "当前服务器已存在一个快照任务正在运行");
        } catch (Throwable ex) {
            LOG.error("Submit a snapshot task unknown exception", ex);
            return Message.failure(ErrorCode.SYSTEM_UNKNOWN_ERROR, "提交快照任务异常失败");
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