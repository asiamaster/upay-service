package com.diligrp.xtrade.upay.trade.util;

import com.diligrp.xtrade.shared.sequence.ISerialKeyGenerator;
import com.diligrp.xtrade.shared.util.DateUtils;
import com.diligrp.xtrade.shared.util.RandomUtils;

import java.time.LocalDate;

/**
 * 支付号生成策略，生成规则：年月日+"1"+两位业务类型码+至少四位顺序数+两位随机码
 */
public class PaymentDatedIdStrategy implements ISerialKeyGenerator.IDatedIdStrategy {

    private Integer type;

    public PaymentDatedIdStrategy(Integer type) {
        this.type = type;
    }

    @Override
    public String id(LocalDate date, long sequence) {
        StringBuilder builder = new StringBuilder();
        String dateFormat = DateUtils.formatDate(date, DateUtils.YYYYMMDD);
        builder.append(dateFormat).append('1').append(type);
        if (sequence < 10) {
            builder.append("000").append(sequence);
        } else if (sequence < 100) {
            builder.append("00").append(sequence);
        } else if (sequence < 1000) {
            builder.append("0").append(sequence);
        } else {
            builder.append(sequence);
        }
        builder.append(RandomUtils.randomNumber(2));

        return builder.toString();
    }
}
