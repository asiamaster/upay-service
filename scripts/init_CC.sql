INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `parent_id`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (7, 'CC', '长春地利农副产品', 0, 70001, 70002, 70003, '长春市', 'unknown', '13688582561', 1, now());

INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 70001, 0, 3, 10, 268435455, '长春地利农副产品', '13688582561', '长春市', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 7, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 70002, 0, 3, 11, 268435455, '长春地利农副产品', '13688582561', '长春市', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 7, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 70003, 0, 3, 12, 268435455, '长春地利农副产品', '13688582561', '长春市', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 7, 0, now());

INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (70001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (70002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (70003, 0, 0, 0, 0, now());

-- 初始化市场参数配置
UPDATE `upay_merchant` SET param = '{"maxPwdErrors": 5}' WHERE mch_id = 7;
-- 初始化风控数据
INSERT INTO `upay_global_permission`(`mch_id`, `deposit`, `withdraw`, `trade`, `created_time`)
VALUES(7, '{"maxAmount":500000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":5000000000}', '{"maxAmount":500000000,"dailyAmount":500000000,"dailyTimes":100,"monthlyAmount":10000000000}', now());
