-- 刷新交易过磅的资金项目历史数据
UPDATE `upay_fund_statement` SET type = 23, type_name='交易手续费' WHERE type IN (1, 2);
UPDATE `upay_payment_fee` SET type = 23, type_name='交易手续费' WHERE type in (1, 2);