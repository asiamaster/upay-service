-- --------------------------------------------------------------------
-- 系统ID生成器数据模型
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `xtrade_sequence_key`;
CREATE TABLE `xtrade_sequence_key` (
  `id` BIGINT NOT NULL,
  `key` VARCHAR(50) NOT NULL COMMENT 'KEY标识',
  `start_with` BIGINT DEFAULT '1' COMMENT '起始值',
  `inc_span` BIGINT DEFAULT '1' COMMENT '跨度',
  `scope` VARCHAR(50) COMMENT '应用范围',
  `version` BIGINT NOT NULL DEFAULT '0' COMMENT '数据版本',
  `expired_date` DATE COMMENT '有效日期',
  `description` VARCHAR(128) COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sequence_key_key` (`key`, `scope`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 商户表
-- 说明：商户表用于维护接入支付的商户，提供商户各专项资金账号管理
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_merchant`;
CREATE TABLE `upay_merchant` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `code` VARCHAR(20) NOT NULL COMMENT '商户编码',
  `name` VARCHAR(80) NOT NULL COMMENT '商户名称',
  `profit_account` BIGINT NOT NULL COMMENT '收益账户',
  `vouch_account` BIGINT NOT NULL COMMENT '担保账户',
  `pledge_account` BIGINT NOT NULL COMMENT '押金账户',
  `address` VARCHAR(128) COMMENT '商户地址',
  `contact` VARCHAR(50) COMMENT '联系人',
  `mobile` VARCHAR(20) COMMENT '手机号',
  `private_key` VARCHAR(512) COMMENT '商户私钥',
  `public_key` VARCHAR(200) COMMENT '商户公钥',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '商户状态',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_merchant_mchId` (`mch_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 商户应用表
-- 说明：商户应用表用于维护商户接入支付的各个应用，控制应用接入权限
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_application`;
CREATE TABLE `upay_application` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_id` BIGINT NOT NULL COMMENT '应用ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `name` VARCHAR(80) NOT NULL COMMENT '应用名称',
  `access_token` VARCHAR(40) COMMENT '授权Token',
  `private_key` VARCHAR(512) COMMENT '应用私钥',
  `public_key` VARCHAR(200) COMMENT '应用公钥',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_application_appId` (`app_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 资金账户表
-- 说明：账号类型分为个人、企业和商户，个人账户和企业账户针对于市场客户；
-- 个人、企业账户业务用途有交易账户，缴费账户，预存款账户；
-- 商户账户为市场特殊账户，业务用途包括：收益资金账户、担保资金账户和押金资金账户等；
-- 资金账号分主资金账号和子资金账号，通过parent_id标识，子资金账号暂时无任何业务用途；
-- parent_id=0为主账号，且登录账号记录资金账号所属的园区卡号。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_fund_account`;
CREATE TABLE `upay_fund_account` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `customer_id` BIGINT NOT NULL COMMENT '客户ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `parent_id` BIGINT NOT NULL COMMENT '父账号ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '账号类型',
  `use_for` TINYINT UNSIGNED NOT NULL COMMENT '业务用途',
  `code` VARCHAR(20) COMMENT '登录账号',
  `name` VARCHAR(40) NOT NULL COMMENT '用户名',
  `gender` TINYINT UNSIGNED COMMENT '性别',
  `mobile` VARCHAR(20) NOT NULL COMMENT '手机号',
  `email` VARCHAR(40) COMMENT '邮箱地址',
  `id_code` VARCHAR(20) COMMENT '身份证号码',
  `address` VARCHAR(128) COMMENT '联系地址',
  `login_pwd` VARCHAR(50) COMMENT '登陆密码',
  `password` VARCHAR(50) NOT NULL COMMENT '交易密码',
  `login_time` DATETIME COMMENT '最近登陆时间',
  `secret_key` VARCHAR(80) NOT NULL COMMENT '安全密钥',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '账号状态',
  `lock_time` DATETIME COMMENT '锁定时间',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_fund_account_accountId` (`account_id`) USING BTREE,
  KEY `idx_fund_account_parentId` (`parent_id`) USING BTREE,
  KEY `idx_fund_account_code` (`code`) USING BTREE,
  KEY `idx_fund_account_name` (`name`) USING BTREE,
  KEY `idx_fund_account_mobile` (`mobile`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 账户资金表
-- 说明：资金冻结时余额不变化，账户余额包含冻结金额，便于资金流水期初余额连贯，
-- 解冻消费时扣减余额；应收金额用于中央结算时记录卖家担保交易应收总金额，
-- 此时还未进行园区-卖家的资金结算
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_account_fund`;
CREATE TABLE `upay_account_fund` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `balance` BIGINT NOT NULL COMMENT '账户余额-分',
  `frozen_amount` BIGINT NOT NULL COMMENT '冻结金额-分',
  `vouch_amount` BIGINT NOT NULL COMMENT '担保金额-分',
  `daily_amount` BIGINT NOT NULL COMMENT '日切金额-分',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_account_fund_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 账户资金流水表
-- 说明：任何一条资金流水（资金变动）都是一次交易订单或交易退款的支付行为产生；
-- 资金流水动作包含收入和支出，支出时流水金额amount为负值，否则为正值；
-- 资金类型包括账户资金、手续费和工本费等，余额balance为期初余额；
-- 资金流水的交易类型标识由哪种业务产生，包括：充值、提现、交易等；
-- 子账号用于标识主账号的资金流水为子账号交易产生，子账号无账户资金。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_fund_statement`;
CREATE TABLE `upay_fund_statement` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `business_id` BIGINT COMMENT '业务账号',
  `trade_type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `action` TINYINT UNSIGNED NOT NULL COMMENT '动作-收入 支出',
  `balance` BIGINT NOT NULL COMMENT '(前)余额-分',
  `amount` BIGINT NOT NULL COMMENT '金额-分(正值 负值)',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '资金类型',
  `type_name` VARCHAR(80) COMMENT '费用描述',
  `description` VARCHAR(128) COMMENT '备注',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_fund_stmt_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_fund_stmt_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 交易订单表
-- 说明：交易订单表用于存储收款方(园区账户)信息，任何一笔交易至少有一个园区账户；
-- max_amount、amount用作处理部分退款，分别记录原始余额和退款后的金额；
-- 资金流水动作包含收入和支出，支出时流水金额amount为负值，否则为正值；
-- 资金类型包括账户资金、手续费和工本费等，余额balance为期初余额；
-- 资金流水的交易类型标识由哪种业务产生，包括：充值、提现、交易等；
-- 外部流水号记录业务系统发起支付时的业务单号，主要用于问题故障排查；
-- 账务周期用于支付系统与业务系统之间的资金对账，查询对账周期的交易明细；
-- 费用金额用于存储向收款方(园区账户)收取的费用。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_trade_order`;
CREATE TABLE `upay_trade_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `mch_id` BIGINT NOT NULL COMMENT '商户ID',
  `app_id` BIGINT NOT NULL COMMENT '应用ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '交易类型',
  `serial_no` VARCHAR(40) COMMENT '外部流水号',
  `cycle_no` VARCHAR(40) COMMENT '账务周期号',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `business_id` BIGINT COMMENT '业务账号',
  `name` VARCHAR(20) COMMENT '账号名称',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `max_amount` BIGINT NOT NULL COMMENT '初始金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用金额-分',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '交易状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_order_tradeId` (`trade_id`) USING BTREE,
  KEY `idx_trade_order_accountId` (`account_id`, `type`) USING BTREE,
  KEY `idx_trade_order_serialNo` (`serial_no`) USING BTREE,
  KEY `idx_trade_order_cycleNo` (`cycle_no`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 交易支付表
-- 说明：交易支付表用于存储付款方信息，付款方包括园区账户、银行、第三方支付渠道等；
-- 数据模型理论上一条交易订单可以有多条支付记录，支付金额可以小于或等于交易订单金额；
-- 所有支付都对应一个支付渠道，即使现金；费用金额用于存储向付款方(园区账户)收取的费用。
-- 组合支付时一个交易订单对应多个支付记录，一个交易订单同一种支付渠道只能有一条记录。
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_trade_payment`;
CREATE TABLE `upay_trade_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '交易ID',
  `channel_id` TINYINT UNSIGNED NOT NULL COMMENT '支付渠道',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `business_id` BIGINT COMMENT '业务账号',
  `name` VARCHAR(20) COMMENT '账号名称',
  `card_no` VARCHAR(20) COMMENT '银行卡号',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用金额-分',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '支付状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_trade_payment_paymentId` (`payment_id`) USING BTREE,
  UNIQUE KEY `idx_trade_payment_tradeId` (`trade_id`, `channel_id`) USING BTREE,
  KEY `idx_trade_payment_accountId` (`account_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 退款表
-- 说明：退款类型包括交易撤销、交易退款和交易冲正，都是对原交易记录的资金逆向操作；
-- 交易退款与交易支付相同都是对交易订单进行的资金操作，一个是支付一个是退款；
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_refund_payment`;
CREATE TABLE `upay_refund_payment` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '退款支付ID',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '退款类型',
  `trade_id` VARCHAR(40) NOT NULL COMMENT '原交易ID',
  `trade_type` TINYINT UNSIGNED NOT NULL COMMENT '原交易类型',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `fee` BIGINT NOT NULL COMMENT '费用金额-分',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '状态',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_payment_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_refund_payment_tradeId` (`trade_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 支付费用表
-- 说明：支付费用表存储交易收取的费用明细，需记录在资金流水表中
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_payment_fee`;
CREATE TABLE `upay_payment_fee` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_id` VARCHAR(40) NOT NULL COMMENT '支付ID',
  `use_for` TINYINT UNSIGNED COMMENT '费用用途',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '费用类型',
  `type_name` VARCHAR(80) COMMENT '费用描述',
  `created_time` DATETIME COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_fee_paymentId` (`payment_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 资金冻结表
-- 说明：支付时进行交易冻结，而非创建交易时冻结资金
-- 资金流水动作包含收入和支出，支出时流水金额amount为负值，否则为正值；
-- 资金类型包括账户资金、手续费和工本费等，余额balance为期初余额；
-- 资金流水的交易类型标识由哪种业务产生，包括：充值、提现、交易等。
-- 创建时间=冻结时间，修改时间=解冻时间，当交易冻结操作人信息为资金账号，否则外部传入
-- --------------------------------------------------------------------
DROP TABLE IF EXISTS `upay_frozen_order`;
CREATE TABLE `upay_frozen_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `frozen_id` BIGINT NOT NULL COMMENT '冻结ID',
  `payment_id` VARCHAR(40) COMMENT '支付ID',
  `account_id` BIGINT NOT NULL COMMENT '账号ID',
  `business_id` BIGINT COMMENT '业务账号',
  `name` VARCHAR(20) COMMENT '用户名',
  `type` TINYINT UNSIGNED NOT NULL COMMENT '冻结类型-系统冻结 交易冻结',
  `amount` BIGINT NOT NULL COMMENT '金额-分',
  `state` TINYINT UNSIGNED NOT NULL COMMENT '冻结状态-冻结 解冻',
  `description` VARCHAR(128) COMMENT '备注',
  `version` INTEGER UNSIGNED NOT NULL COMMENT '数据版本号',
  `created_time` DATETIME COMMENT '创建时间',
  `modified_time` DATETIME COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_frozen_order_frozenId` (`frozen_id`) USING BTREE,
  UNIQUE KEY `uk_frozen_order_paymentId` (`payment_id`) USING BTREE,
  KEY `idx_frozen_order_accountId` (`account_id`, `type`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- 银行和第三方通道数据模型暂不设计
-- --------------------------------------------------------------------