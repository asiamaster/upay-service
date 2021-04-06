-- 资金账号表添加商户ID，用于数据分区(表)存储
ALTER TABLE `upay_fund_account`
ADD COLUMN `mch_id` BIGINT NOT NULL COMMENT '商户ID' AFTER `vouch_amount`;

UPDATE `upay_fund_account` ufa
SET mch_id = (SELECT mch_id FROM `upay_user_account` uua WHERE uua.account_id = ufa.account_id);

-- 寿光市场分区表
DROP TABLE IF EXISTS `upay_fund_statement8`;
CREATE TABLE `upay_fund_statement8` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
    `account_id` BIGINT NOT NULL COMMENT '账号ID',
    `child_id` BIGINT COMMENT '子账号ID',
    `trade_type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
    `action` TINYINT UNSIGNED NOT NULL COMMENT '动作-收入 支出',
    `balance` BIGINT NOT NULL COMMENT '(前)余额-分',
    `amount` BIGINT NOT NULL COMMENT '金额-分(正值 负值)',
    `type` INT NOT NULL COMMENT '资金类型',
    `type_name` VARCHAR(80) COMMENT '费用描述',
    `description` VARCHAR(128) COMMENT '备注',
    `created_time` DATETIME COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_fund_stmt_paymentId` (`payment_id`) USING BTREE,
    KEY `idx_fund_stmt_accountId` (`account_id`, `created_time`) USING BTREE,
    KEY `idx_fund_stmt_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `upay_user_statement9`;
CREATE TABLE `upay_user_statement9` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
    `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
    `channel_id` TINYINT UNSIGNED NOT NULL COMMENT '支付渠道',
    `account_id` BIGINT NOT NULL COMMENT '主账号ID',
    `child_id` BIGINT COMMENT '子账号ID',
    `type` TINYINT UNSIGNED NOT NULL COMMENT '流水类型',
    `type_name` VARCHAR(80) NOT NULL COMMENT '流水说明',
    `amount` BIGINT NOT NULL COMMENT '交易金额-分',
    `fee` BIGINT NOT NULL COMMENT '费用-分',
    `balance` BIGINT NOT NULL COMMENT '期末余额',
    `frozen_amount` BIGINT NOT NULL COMMENT '期末冻结金额',
    `serial_no` VARCHAR(40) COMMENT '业务单号',
    `state` TINYINT UNSIGNED NOT NULL COMMENT '状态',
    `created_time` DATETIME COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_statement_tradeId` (`trade_id`, `account_id`) USING BTREE,
    KEY `idx_user_statement_accountId` (`account_id`, `type`) USING BTREE,
    KEY `idx_user_statement_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 沈阳市场分区表
DROP TABLE IF EXISTS `upay_fund_statement9`;
CREATE TABLE `upay_fund_statement9` (
   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
   `account_id` BIGINT NOT NULL COMMENT '账号ID',
   `child_id` BIGINT COMMENT '子账号ID',
   `trade_type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
   `action` TINYINT UNSIGNED NOT NULL COMMENT '动作-收入 支出',
   `balance` BIGINT NOT NULL COMMENT '(前)余额-分',
   `amount` BIGINT NOT NULL COMMENT '金额-分(正值 负值)',
   `type` INT NOT NULL COMMENT '资金类型',
   `type_name` VARCHAR(80) COMMENT '费用描述',
   `description` VARCHAR(128) COMMENT '备注',
   `created_time` DATETIME COMMENT '创建时间',
   PRIMARY KEY (`id`),
   KEY `idx_fund_stmt_paymentId` (`payment_id`) USING BTREE,
   KEY `idx_fund_stmt_accountId` (`account_id`, `created_time`) USING BTREE,
   KEY `idx_fund_stmt_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `upay_user_statement9`;
CREATE TABLE `upay_user_statement9` (
   `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
   `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
   `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
   `channel_id` TINYINT UNSIGNED NOT NULL COMMENT '支付渠道',
   `account_id` BIGINT NOT NULL COMMENT '主账号ID',
   `child_id` BIGINT COMMENT '子账号ID',
   `type` TINYINT UNSIGNED NOT NULL COMMENT '流水类型',
   `type_name` VARCHAR(80) NOT NULL COMMENT '流水说明',
   `amount` BIGINT NOT NULL COMMENT '交易金额-分',
   `fee` BIGINT NOT NULL COMMENT '费用-分',
   `balance` BIGINT NOT NULL COMMENT '期末余额',
   `frozen_amount` BIGINT NOT NULL COMMENT '期末冻结金额',
   `serial_no` VARCHAR(40) COMMENT '业务单号',
   `state` TINYINT UNSIGNED NOT NULL COMMENT '状态',
   `created_time` DATETIME COMMENT '创建时间',
   PRIMARY KEY (`id`),
   KEY `idx_user_statement_tradeId` (`trade_id`, `account_id`) USING BTREE,
   KEY `idx_user_statement_accountId` (`account_id`, `type`) USING BTREE,
   KEY `idx_user_statement_createdTime` (`created_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;