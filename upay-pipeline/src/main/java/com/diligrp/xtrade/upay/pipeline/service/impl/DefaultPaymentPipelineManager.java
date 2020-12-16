package com.diligrp.xtrade.upay.pipeline.service.impl;

import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.pipeline.dao.IPipelinePaymentDao;
import com.diligrp.xtrade.upay.pipeline.domain.IPipeline;
import com.diligrp.xtrade.upay.pipeline.exception.PaymentPipelineException;
import com.diligrp.xtrade.upay.pipeline.service.IPaymentPipelineManager;
import com.diligrp.xtrade.upay.pipeline.type.Pipeline;
import com.diligrp.xtrade.upay.pipeline.type.PipelineState;
import com.diligrp.xtrade.upay.pipeline.type.PipelineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认支付通道管理器实现
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
@Service("paymentPipelineManager")
public class DefaultPaymentPipelineManager implements IPaymentPipelineManager, BeanPostProcessor {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private Map<PipelineType, IPipeline> pipelines = new ConcurrentHashMap();

    @Resource
    private IPipelinePaymentDao paymentPipelineDao;

    @Override
    public void registerPipeline(PipelineType type, IPipeline pipeline) {
        pipelines.put(type, pipeline);
        LOG.info("{} payment pipeline registered", type.getCode());
    }

    @Override
    public IPipeline loadPipeline(PipelineType type) {
        return Optional.ofNullable(pipelines.get(type)).orElseThrow(
            () -> new PaymentPipelineException(ErrorCode.PIPELINE_NOT_SUPPORTED, "不支持选择的支付通道"));
    }

    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (AopUtils.isAopProxy(bean)) {
            return bean;
        } else {
            Pipeline annotation = bean.getClass().getAnnotation(Pipeline.class);
            if (annotation != null && bean instanceof IPipeline) {
                Optional.ofNullable(annotation.type()).ifPresentOrElse(type -> {
                    IPipeline pipeline = (IPipeline) bean;
                    paymentPipelineDao.findPipelineByCode(type.getCode()).ifPresentOrElse(p -> {
                        if (p.getState() == PipelineState.NORMAL.getCode()) {
                            pipeline.configPipeline(p.getCode(), p.getName(), p.getUri(), p.getParam());
                            registerPipeline(type, (IPipeline) bean);
                        } else {
                            LOG.warn("{} payment pipeline ignored because of abnormal state", bean.getClass().getSimpleName());
                        }
                    }, () -> LOG.warn("{} payment pipeline ignored because of no pipeline configuration", type.getCode()));
                }, () -> {
                    LOG.warn("Payment pipeline ignored because of no pipeline type: {}", bean.getClass().getSimpleName());
                });
            }

            return bean;
        }
    }
}
