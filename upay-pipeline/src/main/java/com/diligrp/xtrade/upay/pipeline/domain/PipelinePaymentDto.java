package com.diligrp.xtrade.upay.pipeline.domain;

import java.time.LocalDateTime;

/**
 * 通道支付申请数据传输对象
 *
 * @author: brenthuang
 * @date: 2020/12/16
 */
public class PipelinePaymentDto {
   // 支付ID
   private String paymentId;
   // 通道流水号
   private String serialNo;
   // 费用金额-分
   private Long fee;
   // 申请状态
   private Integer state;
   // 备注
   private String description;
   // 数据版本号
   private Integer version;
   // 重试次数
   private Integer retryCount;
   // 修改时间
   private LocalDateTime modifiedTime;

   public static PipelinePaymentDto of(String paymentId, String serialNo, Long fee, Integer state,
                                       Integer version, LocalDateTime modifiedTime) {
      PipelinePaymentDto pipelinePayment = new PipelinePaymentDto();
      pipelinePayment.paymentId = paymentId;
      pipelinePayment.serialNo = serialNo;
      pipelinePayment.fee = fee;
      pipelinePayment.state = state;
      pipelinePayment.version = version;
      pipelinePayment.modifiedTime = modifiedTime;
      return pipelinePayment;
   }

   public static PipelinePaymentDto of(String paymentId, Integer state, String description,
                                       Integer version, LocalDateTime modifiedTime) {
      PipelinePaymentDto pipelinePayment = new PipelinePaymentDto();
      pipelinePayment.paymentId = paymentId;
      pipelinePayment.state = state;
      pipelinePayment.description = description;
      pipelinePayment.version = version;
      pipelinePayment.modifiedTime = modifiedTime;
      return pipelinePayment;
   }

   public String getPaymentId() {
      return paymentId;
   }

   public void setPaymentId(String paymentId) {
      this.paymentId = paymentId;
   }

   public String getSerialNo() {
      return serialNo;
   }

   public void setSerialNo(String serialNo) {
      this.serialNo = serialNo;
   }

   public Long getFee() {
      return fee;
   }

   public void setFee(Long fee) {
      this.fee = fee;
   }

   public Integer getState() {
      return state;
   }

   public void setState(Integer state) {
      this.state = state;
   }

   public String getDescription() {
      return description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public Integer getVersion() {
      return version;
   }

   public void setVersion(Integer version) {
      this.version = version;
   }

   public Integer getRetryCount() {
      return retryCount;
   }

   public void setRetryCount(Integer retryCount) {
      this.retryCount = retryCount;
   }

   public LocalDateTime getModifiedTime() {
      return modifiedTime;
   }

   public void setModifiedTime(LocalDateTime modifiedTime) {
      this.modifiedTime = modifiedTime;
   }
}
