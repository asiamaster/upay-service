INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (9, 'SY', '沈阳地利农副产品股份有限公司', 20001, 20002, 20003, '沈阳市大东区', '罗宏伟', '13688582561', 1, now());

INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 20001, 0, 3, 10, 268435455, '沈阳地利农副产品股份有限公司', '13688582561', '沈阳市大东区', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 9, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 20002, 0, 3, 11, 268435455, '沈阳地利农副产品股份有限公司', '13688582561', '沈阳市大东区', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 9, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 20003, 0, 3, 12, 268435455, '沈阳地利农副产品股份有限公司', '13688582561', '沈阳市大东区', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 9, 0, now());

INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (20001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (20002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (20003, 0, 0, 0, 0, now());

-- 初始化支付通道
INSERT INTO `upay_pipeline`(`mch_id`, `code`, `name`, `uri`, `param`, `state`, `created_time`)
VALUES (9, 'SJB_DIRECT', '盛京银行银企直连通道', '127.0.0.1:9527', '{"fromAccount": "123456", "fromName": "沈阳对公户"}', '1', now());
-- 初始化商户允许的支付渠道
INSERT INTO `upay_merchant_channel`(`mch_id`, `channel_id`, `channel_name`, `description`, `created_time`)
VALUES (9, 28, '盛京银行', null, now());