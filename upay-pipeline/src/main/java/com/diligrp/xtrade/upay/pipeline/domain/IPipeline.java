package com.diligrp.xtrade.upay.pipeline.domain;

import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.pipeline.exception.PaymentPipelineException;

/**
 * 支付通道领域模型接口
 *
 * @author: brenthuang
 * @date: 2020/12/09
 */
public interface IPipeline {
    /**
     * 通道编码
     */
    String getCode();

    /**
     * 配置支付通道
     *
     * @param code - 通道编码
     * @param name - 通道名称
     * @param uri - 通道访问地址
     * @param param - 通道参数
     */
    void configPipeline(String code, String name, String uri, String param);

    /**
     * 支付通道发起交易转账申请
     */
    PipelineResponse sendTradeRequest(PipelineRequest request, Callback callback);

    /**
     * 支付通道发起交易查询申请
     */
    PipelineResponse sendQueryRequest(PipelineRequest request, Callback callback);

    /**
     * 支付通道回调接口, 支付通道在不同的阶段回调不同的接口
     */
    interface Callback {
        /**
         * 支付通道建立远程连接超时/失败(配置信息不正确)或通道不可用时回调此接口;
         * 适用范围: TCP协议支付通道建立连接超时/失败, HTTP调用时抛出ConnectTimeoutException时
         *
         * 回调函数可直接向业务层抛出异常并提示支付通道不可用, 此时数据库事务可回滚不保留支付痕迹
         */
        default void connectFailed(PipelineRequest request) {
            throw new PaymentPipelineException(ErrorCode.PIPELINE_NOT_READY, "支付通道不可用");
        }

        /**
         * 支付通道建立远程连接成功时回调此接口;
         * 适用范围: TCP协议支付通道建立远程连接成功, HTTP协议支付通道可直接成功
         *
         * 回调函数发生在发送远程支付请求之前, 任何内部异常都可向业务层抛出异常提示支付失败;
         * 回调函数可在独立的数据库事务中创建"支付中"支付记录, 保存支付痕迹;
         */
        void connectSuccess(PipelineRequest request);

        /**
         * 支付通道获取到第三方支付服务明确的处理结果时回调此接口, 处理结果包括: 支付成功/支付失败/支付处理中;
         *
         * 回调函数发生在发送远程支付请求之后, 执行此回调函数时connectSuccess回调已经执行成功并保存了支付痕迹;
         * 回调函数根据支付通道返回的结果值, 更新支付状态(支付成功/支付失败/支付处理中); 对于"支付处理中"的处理结果
         * 应发起异常处理流程, 比如: 根据某种重试策略去向第三方支付服务查询处理结果并更新本地支付状态
         */
        void pipelineSuccess(PipelineRequest request, PipelineResponse response);

        /**
         * 支付通道等待第三方支付服务返回支付结果异常(接口超时, 网络异常)回调此接口;
         *
         * 回调函数发生在发送远程支付请求之后, 执行此回调函数时connectSuccess回调已经执行成功并保存了支付痕迹;
         * 回调函数可以直接将支付状态修改成"支付处理中"并发起异常处理流程, 比如: 根据某种重试策略去向第三方支付服务
         * 查询处理结果并更新本地支付状态
         */
        void pipelineFailed(PipelineRequest request);
    }
}
