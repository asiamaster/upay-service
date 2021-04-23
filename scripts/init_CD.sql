INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `parent_id`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (3, 'CD', '成都聚合', 0, 90001, 90002, 90003, '成都市', 'unknown', '13688582561', 1, now());

INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 90001, 0, 3, 10, 268435455, '成都聚合', '13688582561', '成都市', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 3, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 90002, 0, 3, 11, 268435455, '成都聚合', '13688582561', '成都市', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 3, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 90003, 0, 3, 12, 268435455, '成都聚合', '13688582561', '成都市', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 3, 0, now());

INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (90001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (90002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (90003, 0, 0, 0, 0, now());

-- 初始化市场参数配置
UPDATE `upay_merchant` SET param = '{"maxPwdErrors": 5}' WHERE mch_id = 3;
-- 初始化风控数据
INSERT INTO `upay_global_permission`(`mch_id`, `deposit`, `withdraw`, `trade`, `created_time`)
VALUES(3, '{"maxAmount":500000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":5000000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":10000000000}', now());