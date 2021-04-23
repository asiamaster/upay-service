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

INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (8, 'SG', '寿光地利农产品集团有限公司', 10001, 10002, 10003, '山东寿光', '赵丽英', '13688582561', 1, now());

INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 10001, 0, 3, 10, 268435455, '寿光地利农产品集团有限公司', '13688582561', '山东寿光', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 8, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 10002, 0, 3, 11, 268435455, '寿光地利农产品集团有限公司', '13688582561', '山东寿光', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 8, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 10003, 0, 3, 12, 268435455, '寿光地利农产品集团有限公司', '13688582561', '山东寿光', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 8, 0, now());

INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (10001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (10002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (10003, 0, 0, 0, 0, now());

-- 寿光赊销缴费免密额度
INSERT INTO `upay_user_protocol`(`protocol_id`, `account_id`, `name`, `type`, `min_amount`, `max_amount`, `start_on`, `state`, `description`, `version`, `created_time`)
VALUES ('9527', 0, 'anonymous', 60, 0, 999900, now(), 1, NULL, 0, now());

-- 寿光市场数据字典配置
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'dataSignSwitch', '接口数据签名验签开关', 'off', 'on-开启签名验签, off-关闭签名验签', NOW());
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'smsNotifySwitch', '短信通知开关', 'off', 'on-开启短信通知, off-关闭短信通知', NOW());
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'maxProtocolAmount30', '进门收费最大免密支付金额', '10000000000', '设置最大免密支付金额', NOW());
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'maxProtocolAmount40', '本地配送最大免密支付金额', '10000', '设置最大免密支付金额', NOW());
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'maxProtocolAmount50', '出门缴费最大免密支付金额', '10000', '设置最大免密支付金额', NOW());
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'maxProtocolAmount60', '赊销缴费最大免密支付金额', '999900', '设置最大免密支付金额', NOW());
-- 初始化风控数据
INSERT INTO `upay_global_permission`(`mch_id`, `deposit`, `withdraw`, `trade`, `created_time`)
VALUES(8, '{"maxAmount":500000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":5000000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":10000000000}', now());