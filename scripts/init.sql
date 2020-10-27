INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (1, 'FUND_ACCOUNT', 100001, 50, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (2, 'TRADE_ID', 100001, 1, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (3, 'PAYMENT_ID', 100001, 1, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (4, 'FROZEN_ID', 100001, 50, NULL, NULL);

INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `app_private_key`, `app_public_key`, `private_key`, `public_key`, `created_time`)
VALUES (1010, 0, '柜台应用', 'abcd1010', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEA1kbf57+InuWrVukfg/uw9QdCMwZ57KlDJa7+TyfrayK3aNIQ2MuknAbc+8M8Np/DlQfa+GMrShXvQeES0r7W+QIDAQABAkBTBULbV6pnZjTsh4ZebLYzOYy8mFXFDA+oGhUONjlQWH1IK5AYYzVbrc6+mVZLi4z1/EW56BSPhYrXZrP2lorBAiEA+IMCdOdK4k12ygzR0UQBVA1QlCXk4T6XIwO4yhCa+UMCIQDcu8fxuIbpWvOoqndBml0E42iZrHI01iky3gAqIvtdEwIgLz1oOCTHfWFQVXQ+ZlNRFVM6oA7cBV1KiaNpey/Q5dUCIB4LEPO9gd9RGcjjKsgrEm4P5bTE2+aFH6ZkwPD7QesxAiBEV7L4LWTPkjsYvC2wdEITi7WwO6dcAaAfpjaiFAOKyg==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANZG3+e/iJ7lq1bpH4P7sPUHQjMGeeypQyWu/k8n62sit2jSENjLpJwG3PvDPDafw5UH2vhjK0oV70HhEtK+1vkCAwEAAQ==',
        'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAkdzZD5Mvnde9pAfIMYf19O6j9v0yMtFCxyIKWS1UvAb4z33iOyaxomYYMc2goTfHTvqVsOhpyhzGSQsTbD8w/QIDAQABAkAUOOgnDqLlYUm7ehC5PT5OTN+SmJvjC7wUW5XPs0cyIhYGiWMlLFhLAf9Sth4fzL3ixDecSSnctIIh3Vf188aJAiEA+JVABJkB9plHCYPPNMQXen9wCJ1wPXV1+vNwCbaXRE8CIQCWNv1o6nSYHdmf0YKm+kqSXGoVJe45IDw2Rum1QpiG8wIhAIrDhgELCLWHysfc9IYYEKMpEHk+qbElKL71tc02SCqxAiAYKuC6cH4xuxu4SszqcHpu8c9fd6rMJhOJ5/7R2tUPYQIgN8nR/F7oNeRhX3LI6lmBmO/uYhfGWn4dNlMNyUPC05U=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHc2Q+TL53XvaQHyDGH9fTuo/b9MjLRQsciClktVLwG+M994jsmsaJmGDHNoKE3x076lbDoacocxkkLE2w/MP0CAwEAAQ==', now());
INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `app_private_key`, `app_public_key`, `private_key`, `public_key`, `created_time`)
VALUES (1020, 0, '进门收费应用', 'abcd1020', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEA1kbf57+InuWrVukfg/uw9QdCMwZ57KlDJa7+TyfrayK3aNIQ2MuknAbc+8M8Np/DlQfa+GMrShXvQeES0r7W+QIDAQABAkBTBULbV6pnZjTsh4ZebLYzOYy8mFXFDA+oGhUONjlQWH1IK5AYYzVbrc6+mVZLi4z1/EW56BSPhYrXZrP2lorBAiEA+IMCdOdK4k12ygzR0UQBVA1QlCXk4T6XIwO4yhCa+UMCIQDcu8fxuIbpWvOoqndBml0E42iZrHI01iky3gAqIvtdEwIgLz1oOCTHfWFQVXQ+ZlNRFVM6oA7cBV1KiaNpey/Q5dUCIB4LEPO9gd9RGcjjKsgrEm4P5bTE2+aFH6ZkwPD7QesxAiBEV7L4LWTPkjsYvC2wdEITi7WwO6dcAaAfpjaiFAOKyg==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANZG3+e/iJ7lq1bpH4P7sPUHQjMGeeypQyWu/k8n62sit2jSENjLpJwG3PvDPDafw5UH2vhjK0oV70HhEtK+1vkCAwEAAQ==',
        'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAkdzZD5Mvnde9pAfIMYf19O6j9v0yMtFCxyIKWS1UvAb4z33iOyaxomYYMc2goTfHTvqVsOhpyhzGSQsTbD8w/QIDAQABAkAUOOgnDqLlYUm7ehC5PT5OTN+SmJvjC7wUW5XPs0cyIhYGiWMlLFhLAf9Sth4fzL3ixDecSSnctIIh3Vf188aJAiEA+JVABJkB9plHCYPPNMQXen9wCJ1wPXV1+vNwCbaXRE8CIQCWNv1o6nSYHdmf0YKm+kqSXGoVJe45IDw2Rum1QpiG8wIhAIrDhgELCLWHysfc9IYYEKMpEHk+qbElKL71tc02SCqxAiAYKuC6cH4xuxu4SszqcHpu8c9fd6rMJhOJ5/7R2tUPYQIgN8nR/F7oNeRhX3LI6lmBmO/uYhfGWn4dNlMNyUPC05U=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHc2Q+TL53XvaQHyDGH9fTuo/b9MjLRQsciClktVLwG+M994jsmsaJmGDHNoKE3x076lbDoacocxkkLE2w/MP0CAwEAAQ==', now());
INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `app_private_key`, `app_public_key`, `private_key`, `public_key`, `created_time`)
VALUES (1030, 0, '综合收费应用', 'abcd1030', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEA1kbf57+InuWrVukfg/uw9QdCMwZ57KlDJa7+TyfrayK3aNIQ2MuknAbc+8M8Np/DlQfa+GMrShXvQeES0r7W+QIDAQABAkBTBULbV6pnZjTsh4ZebLYzOYy8mFXFDA+oGhUONjlQWH1IK5AYYzVbrc6+mVZLi4z1/EW56BSPhYrXZrP2lorBAiEA+IMCdOdK4k12ygzR0UQBVA1QlCXk4T6XIwO4yhCa+UMCIQDcu8fxuIbpWvOoqndBml0E42iZrHI01iky3gAqIvtdEwIgLz1oOCTHfWFQVXQ+ZlNRFVM6oA7cBV1KiaNpey/Q5dUCIB4LEPO9gd9RGcjjKsgrEm4P5bTE2+aFH6ZkwPD7QesxAiBEV7L4LWTPkjsYvC2wdEITi7WwO6dcAaAfpjaiFAOKyg==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANZG3+e/iJ7lq1bpH4P7sPUHQjMGeeypQyWu/k8n62sit2jSENjLpJwG3PvDPDafw5UH2vhjK0oV70HhEtK+1vkCAwEAAQ==',
        'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAkdzZD5Mvnde9pAfIMYf19O6j9v0yMtFCxyIKWS1UvAb4z33iOyaxomYYMc2goTfHTvqVsOhpyhzGSQsTbD8w/QIDAQABAkAUOOgnDqLlYUm7ehC5PT5OTN+SmJvjC7wUW5XPs0cyIhYGiWMlLFhLAf9Sth4fzL3ixDecSSnctIIh3Vf188aJAiEA+JVABJkB9plHCYPPNMQXen9wCJ1wPXV1+vNwCbaXRE8CIQCWNv1o6nSYHdmf0YKm+kqSXGoVJe45IDw2Rum1QpiG8wIhAIrDhgELCLWHysfc9IYYEKMpEHk+qbElKL71tc02SCqxAiAYKuC6cH4xuxu4SszqcHpu8c9fd6rMJhOJ5/7R2tUPYQIgN8nR/F7oNeRhX3LI6lmBmO/uYhfGWn4dNlMNyUPC05U=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHc2Q+TL53XvaQHyDGH9fTuo/b9MjLRQsciClktVLwG+M994jsmsaJmGDHNoKE3x076lbDoacocxkkLE2w/MP0CAwEAAQ==', now());
INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `app_private_key`, `app_public_key`, `private_key`, `public_key`, `created_time`)
VALUES (1040, 0, '商品交易应用', 'abcd1040', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEA1kbf57+InuWrVukfg/uw9QdCMwZ57KlDJa7+TyfrayK3aNIQ2MuknAbc+8M8Np/DlQfa+GMrShXvQeES0r7W+QIDAQABAkBTBULbV6pnZjTsh4ZebLYzOYy8mFXFDA+oGhUONjlQWH1IK5AYYzVbrc6+mVZLi4z1/EW56BSPhYrXZrP2lorBAiEA+IMCdOdK4k12ygzR0UQBVA1QlCXk4T6XIwO4yhCa+UMCIQDcu8fxuIbpWvOoqndBml0E42iZrHI01iky3gAqIvtdEwIgLz1oOCTHfWFQVXQ+ZlNRFVM6oA7cBV1KiaNpey/Q5dUCIB4LEPO9gd9RGcjjKsgrEm4P5bTE2+aFH6ZkwPD7QesxAiBEV7L4LWTPkjsYvC2wdEITi7WwO6dcAaAfpjaiFAOKyg==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANZG3+e/iJ7lq1bpH4P7sPUHQjMGeeypQyWu/k8n62sit2jSENjLpJwG3PvDPDafw5UH2vhjK0oV70HhEtK+1vkCAwEAAQ==',
        'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAkdzZD5Mvnde9pAfIMYf19O6j9v0yMtFCxyIKWS1UvAb4z33iOyaxomYYMc2goTfHTvqVsOhpyhzGSQsTbD8w/QIDAQABAkAUOOgnDqLlYUm7ehC5PT5OTN+SmJvjC7wUW5XPs0cyIhYGiWMlLFhLAf9Sth4fzL3ixDecSSnctIIh3Vf188aJAiEA+JVABJkB9plHCYPPNMQXen9wCJ1wPXV1+vNwCbaXRE8CIQCWNv1o6nSYHdmf0YKm+kqSXGoVJe45IDw2Rum1QpiG8wIhAIrDhgELCLWHysfc9IYYEKMpEHk+qbElKL71tc02SCqxAiAYKuC6cH4xuxu4SszqcHpu8c9fd6rMJhOJ5/7R2tUPYQIgN8nR/F7oNeRhX3LI6lmBmO/uYhfGWn4dNlMNyUPC05U=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAJHc2Q+TL53XvaQHyDGH9fTuo/b9MjLRQsciClktVLwG+M994jsmsaJmGDHNoKE3x076lbDoacocxkkLE2w/MP0CAwEAAQ==', now());

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