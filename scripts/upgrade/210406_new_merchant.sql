-- 新增哈达农副产品股份有限公司
DELETE FROM `upay_merchant` WHERE mch_id = 2;
DELETE FROM `upay_user_account` WHERE `account_id` IN (80001, 80002, 80003);
DELETE FROM `upay_fund_account` WHERE `account_id` IN (80001, 80002, 80003);
INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `parent_id`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (2, 'HD', '哈达农副产品股份有限公司', 0, 80001, 80002, 80003, '哈尔滨市', 'unknown', '13688582561', 1, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 80001, 0, 3, 10, 268435455, '哈达农副产品股份有限公司', '13688582561', '哈尔滨市', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 2, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 80002, 0, 3, 11, 268435455, '哈达农副产品股份有限公司', '13688582561', '哈尔滨市', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 2, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 80003, 0, 3, 12, 268435455, '哈达农副产品股份有限公司', '13688582561', '哈尔滨市', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 2, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (80001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (80002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (80003, 0, 0, 0, 0, now());

-- 新增齐齐哈尔地利农产品
DELETE FROM `upay_merchant` WHERE mch_id = 4;
DELETE FROM `upay_user_account` WHERE `account_id` IN (30001, 30002, 30003);
DELETE FROM `upay_fund_account` WHERE `account_id` IN (30001, 30002, 30003);
INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `parent_id`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (4, 'QQHE', '齐齐哈尔地利农产品', 0, 30001, 30002, 30003, '齐齐哈尔市', 'unknown', '13688582561', 1, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 30001, 0, 3, 10, 268435455, '齐齐哈尔地利农产品', '13688582561', '齐齐哈尔市', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 4, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 30002, 0, 3, 11, 268435455, '齐齐哈尔地利农产品', '13688582561', '齐齐哈尔市', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 4, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 30003, 0, 3, 12, 268435455, '齐齐哈尔地利农产品', '13688582561', '齐齐哈尔市', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 4, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (30001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (30002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (30003, 0, 0, 0, 0, now());

-- 新增牡丹江国际农产品
DELETE FROM `upay_merchant` WHERE mch_id = 5;
DELETE FROM `upay_user_account` WHERE `account_id` IN (50001, 50002, 50003);
DELETE FROM `upay_fund_account` WHERE `account_id` IN (50001, 50002, 50003);
INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `parent_id`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (5, 'MDJ', '牡丹江国际农产品', 0, 50001, 50002, 50003, '牡丹江市', 'unknown', '13688582561', 1, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 50001, 0, 3, 10, 268435455, '牡丹江国际农产品', '13688582561', '牡丹江市', '723c34e4fb337210268ea731f9afea5f7a05c896', 'QkI39VWUCa6CAHI0kFGQ0A==', 1, 5, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 50002, 0, 3, 11, 268435455, '牡丹江国际农产品', '13688582561', '牡丹江市', '6a77ea3cb343c262d2a874629e5bc5f6c707d668', 'znULxxf0SRIfHICEBfefng==', 1, 5, 0, now());
INSERT INTO `upay_user_account`(`customer_id`, `account_id`, `parent_id`, `type`, `use_for`, `permission`, `name`, `mobile`, `address`, `password`, `secret_key`, `state`, `mch_id`, `version`, `created_time`)
VALUES (0, 50003, 0, 3, 12, 268435455, '牡丹江国际农产品', '13688582561', '牡丹江市', '62feeafcaee1e992b9b03bd7494719b11bd0c284', 'XPOmYmU5kjDo9raAO6Zv/Q==', 1, 5, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (50001, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (50002, 0, 0, 0, 0, now());
INSERT INTO `upay_fund_account`(`account_id`, `balance`, `frozen_amount`, `vouch_amount`, `version`, `created_time`)
VALUES (50003, 0, 0, 0, 0, now());

-- 新增贵阳地利农产品
DELETE FROM `upay_merchant` WHERE mch_id = 6;
DELETE FROM `upay_user_account` WHERE `account_id` IN (60001, 60002, 60003);
DELETE FROM `upay_fund_account` WHERE `account_id` IN (60001, 60002, 60003);
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

-- 新增长春地利农副产品
DELETE FROM `upay_merchant` WHERE mch_id = 7;
DELETE FROM `upay_user_account` WHERE `account_id` IN (70001, 70002, 70003);
DELETE FROM `upay_fund_account` WHERE `account_id` IN (70001, 70002, 70003);
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

-- 新增成都聚合市场
DELETE FROM `upay_merchant` WHERE mch_id = 3;
DELETE FROM `upay_user_account` WHERE `account_id` IN (90001, 90002, 90003);
DELETE FROM `upay_fund_account` WHERE `account_id` IN (90001, 90002, 90003);
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