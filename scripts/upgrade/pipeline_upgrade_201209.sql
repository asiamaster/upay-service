-- --------------------------------------------------------------------
-- 新增通道管理配置数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_payment_pipeline`;
DROP TABLE IF EXISTS `upay_pipeline`;
CREATE TABLE `upay_pipeline` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` VARCHAR(20) NOT NULL COMMENT '通道编码',
  `name` VARCHAR(40) NOT NULL COMMENT '费用名称',
  `uri` VARCHAR(60) NOT NULL COMMENT '通道访问URI',
  `param` VARCHAR(250) NULL COMMENT '通道参数',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '通道状态',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY  `uk_payment_pipeline_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 新增通道支付申请数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_pipeline_payment`;
CREATE TABLE `upay_pipeline_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `code` VARCHAR(20) NOT NULL COMMENT '通道编码',
  `to_account` VARCHAR(20) NOT NULL COMMENT '通道账户',
  `to_name` VARCHAR(40) COMMENT '通道账户名',
  `to_type` TINYINT UNSIGNED COMMENT '账户类型',
  `serial_no` VARCHAR(40) COMMENT '通道流水号',
  `amount` BIGINT NOT NULL COMMENT '申请金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用金额-分',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '申请状态',
  `description` VARCHAR(128) COMMENT '备注',
  `retry_count` INTEGER UNSIGNED NOT NULL COMMENT '重试次数',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pipeline_payment_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_pipeline_payment_tradeId` (`trade_id`) USING BTREE,
  KEY `idx_pipeline_payment_serialNo` (`serial_no`) USING BTREE,
  KEY `idx_pipeline_payment_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELETE FROM `upay_pipeline` WHERE `code` = 'SJB';
INSERT INTO `upay_pipeline`(`code`, `name`, `uri`, `param`, `state`, `created_time`)
VALUES ('SJB', '盛京银行', '127.0.0.1:9527', '{"fromAccount": "123456", "fromName": "沈阳对公户"}', '1', now());