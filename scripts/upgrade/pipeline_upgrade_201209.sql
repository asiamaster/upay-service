-- --------------------------------------------------------------------
-- 新增通道管理配置数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_payment_pipeline`;
CREATE TABLE `upay_payment_pipeline` (
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

INSERT INTO upay_payment_pipeline(`code`, `name`, `uri`, `param`, `state`, `created_time`)
VALUES ('SJB', '盛京银行', 'https://proxy.upay.diligrp.com/', null, '1', now());