-- --------------------------------------------------------------------
-- 全局资金风控数据模型
-- 说明：用于对某个商户下的账户资金进行全局风险控制，目前支持充值、提现和交易三大类交易风控；
-- 全局风控采用JSON进行数据存储便于后期扩展，当账户未设置风控时，将使用商户级的全局风控设置；
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_merchant_permission`;
CREATE TABLE `upay_global_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `deposit` VARCHAR(200) NOT NULL COMMENT '充值配置',
  `withdraw` VARCHAR(200) NOT NULL COMMENT '提现配置',
  `trade` VARCHAR(200) NOT NULL COMMENT '交易配置',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_global_permission_mchId` (`mch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 资金风控数据模型
-- 说明：用于账户资金的风险控制，目前支持充值、提现和交易三大类交易风控；
-- 风控可细化至单笔限额、日限额、日次数和月限额等，各类风控采用JSON进行数据存储便于后期扩展；
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_user_permission`;
CREATE TABLE `upay_user_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `deposit` VARCHAR(200) NOT NULL COMMENT '充值配置',
  `withdraw` VARCHAR(200) NOT NULL COMMENT '提现配置',
  `trade` VARCHAR(200) NOT NULL COMMENT '交易配置',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_permission_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 初始化风控数据
INSERT INTO `upay_global_permission`(`mch_id`, `deposit`, `withdraw`, `trade`, `created_time`)
VALUES(8, '{"maxAmount":500000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":5000000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":10000000000}', now());
INSERT INTO `upay_global_permission`(`mch_id`, `deposit`, `withdraw`, `trade`, `created_time`)
VALUES(9, '{"maxAmount":500000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":5000000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":10000000000}', now());