-- --------------------------------------------------------------------
-- 新增通道管理配置数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_pipeline`;
CREATE TABLE `upay_pipeline` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `code` VARCHAR(20) NOT NULL COMMENT '通道编码',
  `name` VARCHAR(40) NOT NULL COMMENT '通道名称',
  `uri` VARCHAR(60) NOT NULL COMMENT '通道访问URI',
  `param` VARCHAR(250) NULL COMMENT '通道参数',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '通道状态',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY  `uk_payment_pipeline_code` (`code`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 通道支付申请数据模型
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
  `bank_no` VARCHAR(20) COMMENT '银行联行行号',
  `bank_name` VARCHAR(60) COMMENT '银行行名',
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

-- --------------------------------------------------------------------
-- 商户支付渠道数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_merchant_channel`;
CREATE TABLE `upay_merchant_channel` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `channel_id` INT NOT NULL COMMENT '支付渠道ID',
  `channel_name` VARCHAR(40) COMMENT '支付渠道名称',
  `description` VARCHAR(128) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_channel_channelId` (`channel_id`, `mch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 银行CardBin基础数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_bank_card`;
CREATE TABLE `upay_bank_card` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bank_code` varchar(10) NOT NULL COMMENT '银行编码',
  `bank_no` varchar(20) NOT NULL COMMENT '联行行号',
  `bank_name` varchar(40) NOT NULL COMMENT '银行名称',
  `full_name` varchar(60) NOT NULL COMMENT '银行全称',
  `bin_no` varchar(20) NOT NULL COMMENT 'BIN编号',
  `bin_name` varchar(40) DEFAULT NULL COMMENT 'BIN名称',
  `bin_length` tinyint(3) unsigned NOT NULL COMMENT 'BIN编号长度',
  `type` char(2) DEFAULT NULL COMMENT '卡类型',
  `length` tinyint(3) unsigned NOT NULL COMMENT '卡号长度',
  PRIMARY KEY (`id`),
  KEY `idx_bank_card_binNo` (`bin_no`,`bin_length`,`length`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 开户行基础数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_bank_info`;
CREATE TABLE `upay_bank_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `bank_no` varchar(20) NOT NULL COMMENT '联行行号',
  `bank_name` varchar(60) NOT NULL COMMENT '联行全称',
  `type_code` varchar(10) NOT NULL COMMENT '行别代码',
  `ibank_no` varchar(20) DEFAULT NULL COMMENT '参与行联行号',
  `node_code` varchar(10) DEFAULT NULL COMMENT '所属节点编码',
  `urbn_code` varchar(10) DEFAULT NULL COMMENT '所在城镇编码',
  PRIMARY KEY (`id`),
  KEY `idx_bank_info_bankName` (`bank_name`,`bank_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DELETE FROM `upay_pipeline` WHERE `mch_id` = '9';
INSERT INTO `upay_pipeline`(`mch_id`, `code`, `name`, `uri`, `param`, `state`, `created_time`)
VALUES (9, 'SJB_DIRECT', '盛京银行银企直连通道', '127.0.0.1:9527', '{"fromAccount": "123456", "fromName": "沈阳对公户"}', '1', now());

DELETE FROM `upay_merchant_channel` WHERE `mch_id` = '9';
INSERT INTO `upay_merchant_channel`(`mch_id`, `channel_id`, `channel_name`, `description`, `created_time`)
VALUES (9, 28, '盛京银行', null, now());