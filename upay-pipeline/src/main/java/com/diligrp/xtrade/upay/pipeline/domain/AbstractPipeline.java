package com.diligrp.xtrade.upay.pipeline.domain;

/**
 * 支付通道抽象模型
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
public abstract class AbstractPipeline implements IPipeline {
    // 通道编码
    protected String code;
    // 通道名称
    protected String name;
    // 通道访问URI
    protected String uri;
    // 通道参数
    protected String param;

    /**
     * 配置支付通道
     *
     * @param code - 通道编码
     * @param name - 通道名称
     * @param uri - 通道访问地址
     * @param param - 通道参数
     */
    public void configPipeline(String code, String name, String uri, String param) {
        this.code = code;
        this.name = name;
        this.uri = uri;
        this.param = param;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getUri() {
        return uri;
    }

    public String getParam() {
        return param;
    }
}
