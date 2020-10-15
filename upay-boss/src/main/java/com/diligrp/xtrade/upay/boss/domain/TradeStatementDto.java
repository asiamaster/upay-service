package com.diligrp.xtrade.upay.boss.domain;

import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.channel.domain.TradeStatement;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.trade.type.TradeType;

import java.time.LocalDateTime;

/**
 * 渠道流水数据传输模型
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public class TradeStatementDto {
    // 交易号
    private String tradeId;
    // 交易类型
    private Integer type;
    // 交易类型描述
    private String typeName;
    // 账号ID
    private Long accountId;
    // 交易金额
    private Long amount;
    // 交易费用
    private Long fee;
    // 交易渠道
    private Integer channelId;
    // 交易时间
    private LocalDateTime when;
    // 交易状态
    private Integer state;
    // 业务单号
    private String serialNo;
    // 退款金额
    private Long refundAmount;
    // 退款时间
    private LocalDateTime refundTime;

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public LocalDateTime getWhen() {
        return when;
    }

    public void setWhen(LocalDateTime when) {
        this.when = when;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public Long getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(Long refundAmount) {
        this.refundAmount = refundAmount;
    }

    public LocalDateTime getRefundTime() {
        return refundTime;
    }

    public void setRefundTime(LocalDateTime refundTime) {
        this.refundTime = refundTime;
    }

    public static TradeStatementDto from(long accountId, TradeStatement trade) {
        TradeStatementDto dto = new TradeStatementDto();
        dto.setTradeId(trade.getTradeId());
        dto.setType(trade.getType());
        typeName(trade, dto);
        dto.setAccountId(accountId);
        amount(accountId, trade, dto);
        dto.setChannelId(trade.getChannelId());
        dto.setWhen(trade.getPayTime());
        dto.setState(trade.getState());
        dto.setSerialNo(trade.getSerialNo());
        dto.setRefundAmount(trade.getRefundAmount());
        dto.setRefundTime(trade.getRefundTime());
        return dto;
    }

    private static void typeName(TradeStatement trade, TradeStatementDto dto) {
        if (trade.getType() == TradeType.DEPOSIT.getCode() || trade.getType() ==  TradeType.BANK_DEPOSIT.getCode()) {
            dto.setTypeName("充值");
        } else if (trade.getType() == TradeType.WITHDRAW.getCode()) {
            dto.setTypeName("提现");
        } else if (trade.getType() == TradeType.PAY_FEE.getCode() || trade.getType() == TradeType.AUTH_FEE.getCode()) {
            dto.setTypeName(ObjectUtils.isNotEmpty(trade.getDescription()) ? trade.getDescription() : "缴费");
        } else if (trade.getType() == TradeType.DIRECT_TRADE.getCode() ||
            trade.getType() == TradeType.AUTH_TRADE.getCode()) {
            dto.setTypeName("交易");
        } else {
            dto.setTypeName(TradeType.getName(trade.getType()));
        }
    }

    private static void amount(long accountId, TradeStatement trade, TradeStatementDto dto) {
        if (trade.getType() == TradeType.DEPOSIT.getCode() || trade.getType() ==  TradeType.BANK_DEPOSIT.getCode()
            || trade.getType() == TradeType.PRE_DEPOSIT.getCode()) {
            dto.setAmount(trade.getAmount() - trade.getFee2());
            dto.setFee(trade.getFee2());
        } else if (trade.getType() == TradeType.WITHDRAW.getCode()) {
            dto.setAmount( - (trade.getAmount() + trade.getFee2()));
            dto.setFee(trade.getFee2());
        } else if (trade.getType() == TradeType.PAY_FEE.getCode() || trade.getType() == TradeType.AUTH_FEE.getCode()) {
            dto.setAmount(-trade.getAmount());
            dto.setFee(0L);
        } else if (trade.getType() == TradeType.REFUND_FEE.getCode()) {
            dto.setAmount(trade.getAmount());
            dto.setFee(0L);
        } else if (trade.getType() == TradeType.DIRECT_TRADE.getCode() ||
            trade.getType() == TradeType.AUTH_TRADE.getCode() || trade.getType() == TradeType.TRANSFER.getCode()) {
            if (accountId == trade.getAccountId1()) {
                dto.setAmount(trade.getAmount() - trade.getFee1());
                dto.setFee(trade.getFee1());
            } else {
                dto.setAmount(-trade.getAmount() - trade.getFee2());
                dto.setFee(trade.getFee2());
            }
        } else {
            throw new PaymentServiceException(ErrorCode.TRADE_NOT_SUPPORTED, "账单中包含未知交易类型");
        }
    }

}