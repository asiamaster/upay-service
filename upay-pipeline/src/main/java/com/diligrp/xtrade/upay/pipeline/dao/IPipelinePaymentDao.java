package com.diligrp.xtrade.upay.pipeline.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.pipeline.model.PaymentPipeline;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePayment;
import com.diligrp.xtrade.upay.pipeline.model.PipelinePaymentDto;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 支付通道数据访问层
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
@Repository("paymentPipelineDao")
public interface IPipelinePaymentDao extends MybatisMapperSupport {

    /**
     * 根据通道编码查询支付通道配置
     * @param code - 通道编码
     * @return 支付通道
     */
    Optional<PaymentPipeline> findPipelineByCode(String code);

    /**
     * 新增通道支付申请
     */
    void insertPipelinePayment(PipelinePayment payment);

    /**
     * 根据支付ID查询通道支付申请
     *
     * @param paymentId - 支付ID
     * @return 通道支付申请
     */
    Optional<PipelinePayment> findPipelinePayment(String paymentId);

    /**
     * 更新通道支付状态
     */
    int compareAndSetState(PipelinePaymentDto payment);

    /**
     * 更新异常重试次数
     */
    int incPipelineTryCount(String paymentId);
}