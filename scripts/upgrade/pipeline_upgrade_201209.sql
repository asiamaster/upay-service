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

INSERT INTO upay_pipeline(`code`, `name`, `uri`, `param`, `state`, `created_time`)
VALUES ('SJB', '盛京银行', '127.0.0.1:9527', '{"fromAccount": "123456", "fromName": "沈阳对公户"}', '1', now());

-- 新增移动端应用
INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `app_private_key`, `app_public_key`, `private_key`, `public_key`, `created_time`)
VALUES (2010, 0, '移动应用', 'abcd2010', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEA1kbf57+InuWrVukfg/uw9QdCMwZ57KlDJa7+TyfrayK3aNIQ2MuknAbc+8M8Np/DlQfa+GMrShXvQeES0r7W+QIDAQABAkBTBULbV6pnZjTsh4ZebLYzOYy8mFXFDA+oGhUONjlQWH1IK5AYYzVbrc6+mVZLi4z1/EW56BSPhYrXZrP2lorBAiEA+IMCdOdK4k12ygzR0UQBVA1QlCXk4T6XIwO4yhCa+UMCIQDcu8fxuIbpWvOoqndBml0E42iZrHI01iky3gAqIvtdEwIgLz1oOCTHfWFQVXQ+ZlNRFVM6oA7cBV1KiaNpey/Q5dUCIB4LEPO9gd9RGcjjKsgrEm4P5bTE2+aFH6ZkwPD7QesxAiBEV7L4LWTPkjsYvC2wdEITi7WwO6dcAaAfpjaiFAOKyg==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANZG3+e/iJ7lq1bpH4P7sPUHQjMGeeypQyWu/k8n62sit2jSENjLpJwG3PvDPDafw5UH2vhjK0oV70HhEtK+1vkCAwEAAQ==',
        'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAkdzZD5Mvnde9pAfIMYf19O6j9v0yMtFCxyIKWS1UvAb4z33iOyaxomYYMc2goTfHTvqVsOhpyhzGSQsTbD8w/QIDAQABAkAUOOgnDqLlYUm7ehC5PT5OTN+SmJvjC7wUW5XPs0cyIhYGiWMlLFhLAf9Sth4fzL3ixDecSSnctIIh3Vf188aJAiEA+JVABJkB9plHCYPPNMQXen9wCJ1wPXV1+vNwCbaXRE8CIQCWNv1o6nSYHdmf0YKm+kqSXGoVJe45IDw2Rum1QpiG8wIhAIrDhgELCLWHysfc9IYYEKMpEHk+qbElKL71tc02SCqxAiAYKuC6cH4xuxu4SszqcHpu8c9fd6rMJhOJ5/7R2tUPYQIgN8nR/F7oNeRhX3LI6lmBmO/uYhfGWn4dNlMNyUPC05U=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHc2Q+TL53XvaQHyDGH9fTuo/b9MjLRQsciClktVLwG+M994jsmsaJmGDHNoKE3x076lbDoacocxkkLE2w/MP0CAwEAAQ==', now());