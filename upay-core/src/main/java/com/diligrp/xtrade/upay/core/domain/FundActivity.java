package com.diligrp.xtrade.upay.core.domain;

/**
 * 资金活动（金额/资金动作-收或支/资金项类型）模型
 */
public class FundActivity {
    // 金额 - 分（正数或负数）
    private long amount;
    // 资金类型
    private int type;
    // 资金描述
    private String typeName;

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public static int compare(FundActivity a, FundActivity b) {
        int valueA = a.getAmount() > 0 ? 1 : 2;
        int valueB = b.getAmount() > 0 ? 1 : 2;
        return valueA - valueB;
    }

    public static FundActivity of(long amount, int type, String typeName) {
        FundActivity activity = new FundActivity();
        activity.setAmount(amount);
        activity.setType(type);
        activity.setTypeName(typeName);
        return activity;
    }
}
