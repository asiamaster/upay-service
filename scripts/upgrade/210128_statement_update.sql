-- 刷新交易过磅的资金项目历史数据
UPDATE `upay_fund_statement` SET type = 23, type_name='交易手续费' WHERE type IN (1, 2);
UPDATE `upay_payment_fee` SET type = 23, type_name='交易手续费' WHERE type in (1, 2);
-- 刷新撤销即时交易时, 用户流水payment_id错误数据
UPDATE upay_user_statement uus SET payment_id = (SELECT payment_id FROM upay_refund_payment urp WHERE uus.trade_id=urp.trade_id) WHERE type_name='交易-退款';