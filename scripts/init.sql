INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (1, 'FUND_ACCOUNT', 100001, 50, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (2, 'TRADE_ID', 100001, 1, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (3, 'PAYMENT_ID', 100001, 1, NULL, NULL);
INSERT INTO `xtrade_sequence_key`(`id`, `key`, `start_with`, `inc_span`, `scope`, `description`) VALUES (4, 'FROZEN_ID', 100001, 50, NULL, NULL);

-- INSERT INTO `upay_merchant`(`mch_id`, `code`, `name`, `profit_account`, `vouch_account`, `return_account`, `address`, `contact`, `mobile`, `state`)
-- VALUES (1000, 'HRB', '哈尔滨市场', 0, 0, 0, '哈尔滨', '管理员', '13600000000', 1);
-- INSERT INTO `upay_application`(`app_id`, `mch_id`, `name`, `access_token`, `secret_key`)
-- VALUES (1010, 1000, '电子结算系统', '12345678', null);