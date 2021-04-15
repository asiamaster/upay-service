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

DROP TABLE IF EXISTS `upay_user_statement8`;
CREATE TABLE `upay_user_statement8` (
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

INSERT INTO upay_fund_statement8(`payment_id`, `account_id`, `child_id`, `trade_type`, `action`, `balance`, `amount`, `type`, `type_name`, `description`, `created_time`)
SELECT ufs.`payment_id`, ufs.`account_id`, ufs.`child_id`, ufs.`trade_type`, ufs.`action`, ufs.`balance`, ufs.`amount`, ufs.`type`, ufs.`type_name`, ufs.`description`, ufs.`created_time` FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=8;

DELETE ufs FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=8;

INSERT INTO upay_user_statement8(`trade_id`, `payment_id`, `channel_id`, `account_id`, `child_id`, `type`, `type_name`, `amount`, `fee`, `balance`, `frozen_amount`, `serial_no`, `state`, `created_time`)
SELECT uus.`trade_id`, uus.`payment_id`, uus.`channel_id`, uus.`account_id`, uus.`child_id`, uus.`type`, uus.`type_name`, uus.`amount`, uus.`fee`, uus.`balance`, uus.`frozen_amount`, uus.`serial_no`, uus.`state`, uus.`created_time` FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=8;

DELETE uus FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=8;

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

INSERT INTO upay_fund_statement9(`payment_id`, `account_id`, `child_id`, `trade_type`, `action`, `balance`, `amount`, `type`, `type_name`, `description`, `created_time`)
SELECT ufs.`payment_id`, ufs.`account_id`, ufs.`child_id`, ufs.`trade_type`, ufs.`action`, ufs.`balance`, ufs.`amount`, ufs.`type`, ufs.`type_name`, ufs.`description`, ufs.`created_time` FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=9;

DELETE ufs FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=9;

INSERT INTO upay_user_statement9(`trade_id`, `payment_id`, `channel_id`, `account_id`, `child_id`, `type`, `type_name`, `amount`, `fee`, `balance`, `frozen_amount`, `serial_no`, `state`, `created_time`)
SELECT uus.`trade_id`, uus.`payment_id`, uus.`channel_id`, uus.`account_id`, uus.`child_id`, uus.`type`, uus.`type_name`, uus.`amount`, uus.`fee`, uus.`balance`, uus.`frozen_amount`, uus.`serial_no`, uus.`state`, uus.`created_time` FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=9;

DELETE uus FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=9;

-- 哈尔滨市场分区表
DROP TABLE IF EXISTS `upay_fund_statement2`;
CREATE TABLE `upay_fund_statement2` (
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

DROP TABLE IF EXISTS `upay_user_statement2`;
CREATE TABLE `upay_user_statement2` (
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

INSERT INTO upay_fund_statement2(`payment_id`, `account_id`, `child_id`, `trade_type`, `action`, `balance`, `amount`, `type`, `type_name`, `description`, `created_time`)
SELECT ufs.`payment_id`, ufs.`account_id`, ufs.`child_id`, ufs.`trade_type`, ufs.`action`, ufs.`balance`, ufs.`amount`, ufs.`type`, ufs.`type_name`, ufs.`description`, ufs.`created_time` FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=2;

DELETE ufs FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=2;

INSERT INTO upay_user_statement2(`trade_id`, `payment_id`, `channel_id`, `account_id`, `child_id`, `type`, `type_name`, `amount`, `fee`, `balance`, `frozen_amount`, `serial_no`, `state`, `created_time`)
SELECT uus.`trade_id`, uus.`payment_id`, uus.`channel_id`, uus.`account_id`, uus.`child_id`, uus.`type`, uus.`type_name`, uus.`amount`, uus.`fee`, uus.`balance`, uus.`frozen_amount`, uus.`serial_no`, uus.`state`, uus.`created_time` FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=2;

DELETE uus FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=2;

-- 贵阳市场分区表
DROP TABLE IF EXISTS `upay_fund_statement6`;
CREATE TABLE `upay_fund_statement6` (
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

DROP TABLE IF EXISTS `upay_user_statement6`;
CREATE TABLE `upay_user_statement6` (
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

INSERT INTO upay_fund_statement6(`payment_id`, `account_id`, `child_id`, `trade_type`, `action`, `balance`, `amount`, `type`, `type_name`, `description`, `created_time`)
SELECT ufs.`payment_id`, ufs.`account_id`, ufs.`child_id`, ufs.`trade_type`, ufs.`action`, ufs.`balance`, ufs.`amount`, ufs.`type`, ufs.`type_name`, ufs.`description`, ufs.`created_time` FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=6;

DELETE ufs FROM upay_fund_statement ufs INNER JOIN upay_fund_account ufa ON ufs.account_id = ufa.account_id WHERE ufa.mch_id=6;

INSERT INTO upay_user_statement6(`trade_id`, `payment_id`, `channel_id`, `account_id`, `child_id`, `type`, `type_name`, `amount`, `fee`, `balance`, `frozen_amount`, `serial_no`, `state`, `created_time`)
SELECT uus.`trade_id`, uus.`payment_id`, uus.`channel_id`, uus.`account_id`, uus.`child_id`, uus.`type`, uus.`type_name`, uus.`amount`, uus.`fee`, uus.`balance`, uus.`frozen_amount`, uus.`serial_no`, uus.`state`, uus.`created_time` FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=6;

DELETE uus FROM upay_user_statement uus INNER JOIN upay_fund_account ufa ON uus.account_id = ufa.account_id WHERE ufa.mch_id=6;