package com.diligrp.xtrade.upay.channel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.channel.model.DailySnapshot;
import com.diligrp.xtrade.upay.channel.model.SnapshotGuard;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 账户余额快照数据访问层
 *
 * @author: brenthuang
 * @date: 2020/07/29
 */
@Repository("accountSnapshotDao")
public interface IAccountSnapshotDao extends MybatisMapperSupport {
    /**
     * 插入快照监视数据
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void insertSnapshotGuard(SnapshotGuard guard);

    /**
     * 查找快照监视数据
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    SnapshotGuard findSnapshotGuard(@Param("snapshotOn") LocalDate dayOn, @Param("state") Integer state);

    /**
     * 查找指定时间内最新的快照监视数据
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
    SnapshotGuard findLastSnapshotGuard(@Param("snapshotOn") LocalDate dayOn, @Param("state") Integer state);

    /**
     * 修改快照监视数据状态
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    int updateSnapshotGuardState(SnapshotGuard guard);

    /**
     * 批量插入快照数据
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void batchInsertDailySnapshot(List<DailySnapshot> snapshots);
}
