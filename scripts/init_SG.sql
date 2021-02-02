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