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

INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `parent_id`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (6, 'GY', '贵阳地利农产品', 0, 60001, 60002, 60003, '贵阳市', 'unknown', '13688582561', 1, now());

INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 60001, 0, 3, 10, 268435455, '贵阳地利农产品', '13688582561', '贵阳市', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 6, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 60002, 0, 3, 11, 268435455, '贵阳地利农产品', '13688582561', '贵阳市', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 6, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 60003, 0, 3, 12, 268435455, '贵阳地利农产品', '13688582561', '贵阳市', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 6, 0, now());

INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (60001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (60002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (60003, 0, 0, 0, 0, now());

-- 初始化市场参数配置
UPDATE `upay_merchant` SET param = '{"maxPwdErrors": 5}' WHERE mch_id = 6;
-- 初始化风控数据
INSERT INTO `upay_global_permission`(`mch_id`, `deposit`, `withdraw`, `trade`, `created_time`)
VALUES(6, '{"maxAmount":500000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":5000000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":10000000000}', now());