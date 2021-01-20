-- --------------------------------------------------------------------
-- 商户模型新增主子商户关系
-- --------------------------------------------------------------------
ALTER TABLE `upay_merchant` ADD COLUMN `parent_id` BIGINT NOT NULL COMMENT '父商户ID' AFTER `name`;
ALTER TABLE `upay_merchant` ADD COLUMN `param` VARCHAR(120) NULL COMMENT '参数配置' AFTER `pledge_account`;
UPDATE `upay_merchant` SET `parent_id` = 0;
UPDATE `upay_merchant` SET param = '{"maxPwdErrors": 5}' WHERE `code` = 'SY';

-- 新增沈阳市场
DELETE FROM `upay_merchant` WHERE mch_id = 9;
DELETE FROM `upay_user_account` WHERE `account_id` IN (20001, 20002, 20003);
DELETE FROM `upay_fund_account` WHERE `account_id` IN (20001, 20002, 20003);

INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `parent_id`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `state`, `created_time`)
VALUES (9, 'SY', '沈阳地利农副产品股份有限公司', 0, 20001, 20002, 20003, '沈阳市大东区', '罗宏伟', '13688582561', 1, now());
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

-- 新增移动端应用
DELETE FROM `upay_application` WHERE `app_id` = 2010;
INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `app_private_key`, `app_public_key`, `private_key`, `public_key`, `created_time`)
VALUES (2010, 0, '移动应用', 'abcd2010', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEA1kbf57+InuWrVukfg/uw9QdCMwZ57KlDJa7+TyfrayK3aNIQ2MuknAbc+8M8Np/DlQfa+GMrShXvQeES0r7W+QIDAQABAkBTBULbV6pnZjTsh4ZebLYzOYy8mFXFDA+oGhUONjlQWH1IK5AYYzVbrc6+mVZLi4z1/EW56BSPhYrXZrP2lorBAiEA+IMCdOdK4k12ygzR0UQBVA1QlCXk4T6XIwO4yhCa+UMCIQDcu8fxuIbpWvOoqndBml0E42iZrHI01iky3gAqIvtdEwIgLz1oOCTHfWFQVXQ+ZlNRFVM6oA7cBV1KiaNpey/Q5dUCIB4LEPO9gd9RGcjjKsgrEm4P5bTE2+aFH6ZkwPD7QesxAiBEV7L4LWTPkjsYvC2wdEITi7WwO6dcAaAfpjaiFAOKyg==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANZG3+e/iJ7lq1bpH4P7sPUHQjMGeeypQyWu/k8n62sit2jSENjLpJwG3PvDPDafw5UH2vhjK0oV70HhEtK+1vkCAwEAAQ==',
        'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAkdzZD5Mvnde9pAfIMYf19O6j9v0yMtFCxyIKWS1UvAb4z33iOyaxomYYMc2goTfHTvqVsOhpyhzGSQsTbD8w/QIDAQABAkAUOOgnDqLlYUm7ehC5PT5OTN+SmJvjC7wUW5XPs0cyIhYGiWMlLFhLAf9Sth4fzL3ixDecSSnctIIh3Vf188aJAiEA+JVABJkB9plHCYPPNMQXen9wCJ1wPXV1+vNwCbaXRE8CIQCWNv1o6nSYHdmf0YKm+kqSXGoVJe45IDw2Rum1QpiG8wIhAIrDhgELCLWHysfc9IYYEKMpEHk+qbElKL71tc02SCqxAiAYKuC6cH4xuxu4SszqcHpu8c9fd6rMJhOJ5/7R2tUPYQIgN8nR/F7oNeRhX3LI6lmBmO/uYhfGWn4dNlMNyUPC05U=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHc2Q+TL53XvaQHyDGH9fTuo/b9MjLRQsciClktVLwG+M994jsmsaJmGDHNoKE3x076lbDoacocxkkLE2w/MP0CAwEAAQ==', now());