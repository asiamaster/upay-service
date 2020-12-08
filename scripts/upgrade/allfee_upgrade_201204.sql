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
  `type_name` VARCHAR(80) COMMENT '费用描述',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_deduct_fee_paymentId` (`payment_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 0, 0, 1, 1, 268435455, '虚拟客户', '13688582561', '中国地利集团', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 0, 0, now());
