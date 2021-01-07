package com.diligrp.xtrade.upay.pipeline.model;

import com.diligrp.xtrade.shared.domain.BaseDo;

/**
 * 支付通道数据模型
 *
 * @author: brenthuang
 * @date: 2020/12/10
 */
public class PaymentPipeline extends BaseDo {
    // 通道所属商户
    private Long mchId;
    // 通道编码
    private String code;
    // 通道名称
    private String name;
    // 通道所属渠道
    private Integer channelId;
    // 通道访问URI
    private String uri;
    // 通道参数
    private String param;
    // 通道状态
    private Integer state;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
