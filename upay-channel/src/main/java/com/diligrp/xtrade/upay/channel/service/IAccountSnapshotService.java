package com.diligrp.xtrade.upay.channel.service;

import java.time.LocalDate;

/**
 * 账户余额快照服务接口
 *
 * @author: brenthuang
 * @date: 2020/07/29
 */
public interface IAccountSnapshotService {
    /**
     * 记录账户余额快照
     */
    void makeAccountSnapshot(LocalDate snapshotOn);
}
