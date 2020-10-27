package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.upay.channel.model.UserStatement;

/**
 * 渠道流水数据模型
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public class UserStatementDto extends UserStatement {
    // 退款总金额
    private Long refundAmount;

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }
}
