package com.diligrp.xtrade.upay.channel.domain;

import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.core.domain.FundActivity;
import com.diligrp.xtrade.upay.core.domain.FundTransaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 账户/余额渠道领域模型
 */
public class AccountChannel {
    // 支付ID
    private String paymentId;
    // 资金账号ID
    private long accountId;
    // 业务账号ID
    private Long businessId;

    public AccountChannel(String paymentId, long accountId, Long businessId) {
        this.paymentId = paymentId;
        this.accountId = accountId;
        this.businessId = businessId;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public long getAccountId() {
        return accountId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public IFundTransaction openTransaction(int tradeType, LocalDateTime when) {
        return new ChannelFundTransaction(tradeType, when);
    }

    public static AccountChannel of(String paymentId, long accountId, Long businessId) {
        return new AccountChannel(paymentId, accountId, businessId);
    }

    public class ChannelFundTransaction implements IFundTransaction {
        // 业务类型 - 资金冻结时不使用
        private int tradeType;
        // 冻结金额 - 正数时为资金冻结, 负数时为资金解冻
        private long frozenAmount = 0;
        // 资金流
        private List<FundActivity> funds = new ArrayList<>();
        // 发生时间
        private LocalDateTime when;

        public ChannelFundTransaction(int tradeType, LocalDateTime when) {
            this.tradeType = tradeType;
            this.when = when;
        }

        @Override
        public void income(long amount, int type, String typeName) {
            AssertUtils.isTrue(amount > 0, "Invalid amount");
            funds.add(FundActivity.of(amount, type, typeName));
        }

        @Override
        public void outgo(long amount, int type, String typeName) {
            AssertUtils.isTrue(amount > 0, "Invalid amount");
            funds.add(FundActivity.of(-amount, type, typeName));
        }

        @Override
        public void freeze(long amount) {
            AssertUtils.isTrue(amount > 0, "Invalid amount");
            this.frozenAmount =+ amount;
        }

        @Override
        public void unfreeze(long amount) {
            AssertUtils.isTrue(amount > 0, "Invalid amount");
            this.frozenAmount =- amount;
        }

        @Override
        public Optional<FundTransaction> fundTransaction() {
            if (!funds.isEmpty()) {
                AssertUtils.notEmpty(paymentId, "paymentId missed");
            } else if (frozenAmount == 0) {
                return Optional.ofNullable(null);
            }

            FundActivity[] fundActivities = funds.toArray(new FundActivity[funds.size()]);
            return Optional.of(FundTransaction.of(paymentId, accountId, businessId, tradeType, frozenAmount, fundActivities, when));
        }
    }
}