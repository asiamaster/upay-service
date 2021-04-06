-- --------------------------------------------------------------------
-- 综合收费功能数据模型调整, 综合收费允许使用订金进行抵扣
-- 1. 新增抵扣费用数据模型
-- 2. 新增虚拟客户, 用于处理杭州市场无账户进行缴费的情况, 目的是为了保证系统处理逻辑一致
-- --------------------------------------------------------------------

DROP TABLE IF EXISTS `upay_deduct_fee`;
CREATE TABLE `upay_deduct_fee` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `use_for` TINYINT UNSIGNED COMMENT '费用用途',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `type` INT NOT NULL COMMENT '费用类型',
  `type_name` VARCHAR(60) COMMENT '类型说明',
  `description` VARCHAR(80) COMMENT '费用描述',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_deduct_fee_paymentId` (`payment_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 增加费用描述
ALTER TABLE `upay_payment_fee`
MODIFY COLUMN `type_name` VARCHAR(60) COMMENT '类型说明' AFTER `type`,
ADD COLUMN `description` VARCHAR(80) COMMENT '费用描述' AFTER `type_name`;

DELETE FROM `upay_user_account` WHERE `account_id` = 0;
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 0, 0, 1, 1, 268435455, 'anonymous', '13688582561', '中国地利集团', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 0, 0, now());

-- 寿光赊销缴费免密额度
INSERT INTO `upay_user_protocol`(`protocol_id`, `account_id`, `name`, `type`, `min_amount`, `max_amount`, `start_on`, `state`, `description`, `version`, `created_time`)
VALUES ('9527', 0, 'anonymous', 60, 0, 999900, now(), 1, NULL, 0, now());
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'maxProtocolAmount60', '赊销缴费最大免密支付金额', '999900', '设置最大免密支付金额', NOW());