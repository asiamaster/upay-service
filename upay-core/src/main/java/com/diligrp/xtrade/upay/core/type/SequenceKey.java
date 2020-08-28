package com.diligrp.xtrade.upay.core.type;

import com.diligrp.xtrade.shared.sequence.SnowflakeKeyManager;

/**
 * ID生成器使用的ID类型列表
 */
public enum SequenceKey implements SnowflakeKeyManager.SnowflakeKey {
    FUND_ACCOUNT,

    TRADE_ID,

    PAYMENT_ID,

    FROZEN_ID;
}
