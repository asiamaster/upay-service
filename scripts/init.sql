INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (1, 'FUND_ACCOUNT', 100001, 50, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (2, 'TRADE_ID', 100001, 1, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (3, 'PAYMENT_ID', 100001, 1, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (4, 'FROZEN_ID', 100001, 50, NULL, NULL);

INSERT INTO `upay_application`(`id`, `app_id`, `mch_id`, `name`, `access_token`, `private_key`, `public_key`, `created_time`, `modified_time`)
VALUES (2, 100101, 1001, '进门系统', 'abcd1234', 'MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAgJh3Ra/+pCLGh4N+ZJmU5Jhp0zhdCCkAepfNRo5ns8tppDt2xVL5A9c1h8+lzL7sHuXb0C/2IfNRY/azcVejiQIDAQABAkBzqq/9mqPsdusjkP7q9cmbJAxr1FK5uEeZ/VjCkextJsJSDYErwiKok5esPMbooFiLgj+idFZ9cFlmh1TULhhBAiEA8JBuFIQnixy8g53G+THX6cyrs/uLtAVmTIhcIM/sl40CIQCI2M9v3j5Rl91Adp4Non8xhP29/A6eRo8/Xnm+cWQu7QIhAI++PYiUzuwY56vdgx8z4UgavB53mCqhb1cbw7D3jO+hAiBucFroczlj6+V5EsF3S37O3f9RhcveXB9bCsnBsg0h+QIgV//L6wMjYL4wWy+qOvwE7ifrHxxDJAzo3yrjMDKBExA=', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAICYd0Wv/qQixoeDfmSZlOSYadM4XQgpAHqXzUaOZ7PLaaQ7dsVS+QPXNYfPpcy+7B7l29Av9iHzUWP2s3FXo4kCAwEAAQ==', '2020-06-22 17:11:10', NULL);
INSERT INTO `upay_merchant`(`id`, `mch_id`, `code`, `name`, `profit_account`, `vouch_account`, `pledge_account`, `address`, `contact`, `mobile`, `private_key`, `public_key`, `state`, `created_time`, `modified_time`)
VALUES (6, 1001, 'SG', '寿光地利农产品集团有限公司', 100101, 100102, 100103, '山东寿光', '赵丽英', '13688582561', 'MIIBUwIBADANBgkqhkiG9w0BAQEFAASCAT0wggE5AgEAAkEAvObqxcDMEHdvDNppnzdIL/QH2B724Qo4OU3S4NMnCDzBeUsx8fZmQUC3BlwVIM0CRHlIhFb1cnY8Zz1EF/FUvQIDAQABAkBO0kPQNDxx+oP55wD/kH+skrTbN0Bocm03bAO8EB9PTeJGO4K3jKYZi935O5q36AmhewhgcWchA0xy355VBMPBAiEA8iCKauYyG4NBqK+xzNz/PPxRuKchnmmRAmEeOrrKFHkCIQDHua8md6LWUg7wCMzO2b/dHuuTIHarLBJt2P+DsbcJZQIgY6MictUhv5Km/Vy3pR84ZYdLtdoJCqBUjQ8cGnztVbkCIFHFZECLms/EqPj9oVzPKg6OKkUCdZN/Z8wxm+t/R0FBAiBXiYl+UCfjQUPr+P2/mkbo0ef/ildchXuJ1rOqT+L7UQ==', 'MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALzm6sXAzBB3bwzaaZ83SC/0B9ge9uEKODlN0uDTJwg8wXlLMfH2ZkFAtwZcFSDNAkR5SIRW9XJ2PGc9RBfxVL0CAwEAAQ==', 1, '2020-06-22 17:10:19', NULL);

-- 寿光市场数据字典配置
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'dataSignSwitch', '接口数据签名验签开关', 'off', 'on-开启签名验签, off-关闭签名验签', NOW());
INSERT INTO upay_data_dictionary(type, group_code, code, name, value, description, created_time)
VALUES (1, 'SG', 'smsNotifySwitch', '短信通知开关', 'off', 'on-开启短信通知, off-关闭短信通知', NOW());