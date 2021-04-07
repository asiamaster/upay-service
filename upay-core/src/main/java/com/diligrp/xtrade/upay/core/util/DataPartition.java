package com.diligrp.xtrade.upay.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据分区信息
 *
 * @author: brenthuang
 * @date: 2021/04/07
 */
public class DataPartition {

    private static Map<Long, DataPartition> strategies = new ConcurrentHashMap<>();

    static {
        register(DataPartition.of(8L, "8"));
        register(DataPartition.of(9L, "9"));
        register(DataPartition.of(11L, "11"));
    }

    // 商户ID
    private Long mchId;
    // 数据分区信息
    private String partition;

    public Long getMchId() {
        return mchId;
    }

    public void setMchId(Long mchId) {
        this.mchId = mchId;
    }

    public String getPartition() {
        return partition;
    }

    public void setPartition(String partition) {
        this.partition = partition;
    }

    public static DataPartition strategy(Long mchId) {
        DataPartition partition = strategies.get(mchId);
        return partition != null ? partition : DataPartition.of(mchId, "");
    }

    private static DataPartition of(Long mchId, String partition) {
        DataPartition dataPartition = new DataPartition();
        dataPartition.setMchId(mchId);
        dataPartition.setPartition(partition);
        return dataPartition;
    }

    private static void register(DataPartition partition) {
        strategies.put(partition.getMchId(), partition);
    }
}
