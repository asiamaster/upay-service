package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.upay.channel.dao.IAccountSnapshotDao;
import com.diligrp.xtrade.upay.channel.model.DailySnapshot;
import com.diligrp.xtrade.upay.channel.model.SnapshotGuard;
import com.diligrp.xtrade.upay.channel.service.IAccountSnapshotService;
import org.apache.ibatis.session.ResultContext;
import org.apache.ibatis.session.ResultHandler;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账户余额快照服务实现
 *
 * @author: brenthuang
 * @date: 2020/07/29
 */
@Service("accountSnapshotService")
public class AccountSnapshotServiceImpl implements IAccountSnapshotService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final String MAPPER_STATEMENT = "com.diligrp.xtrade.upay.channel.dao.IAccountSnapshotDao.listAccountSnapshot";

    @Resource
    private IAccountSnapshotDao accountSnapshotDao;

    @Resource
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * {@inheritDoc}
     *
     * 账户余额快照总体技术方案：余额快照采用增量快照的方式，即本次快照余额(T2)=上次快照余额(T1)+ 增量交易金额(T1<->T2);
     * 快照监视snapshot_guard负责记录每次快照日志，用于防止重复快照，更重要的是用于查询距离本次最近的快照余额信息;
     * 利用收支明细表和冻结订单表获取增量交易金额，通过上次快照信息+增量交易金额得到本次快照信息。
     *
     * 快照记录允许失败或部分成功，因此该方法不需要运行在数据库事务中，每个Dao方法自行控制事务
     *
     * @param snapshotOn - 快照日期, 余额快照记录的时指定日期(snapshotOn)23:59:59的余额
     */
    @Override
    @Transactional(propagation = Propagation.NEVER)
    public void makeAccountSnapshot(LocalDate snapshotOn) {
        if (!snapshotOn.isBefore(LocalDate.now())) {
            LOG.warn("Cannot take a snapshot on or after today, skip this snapshot");
            return;
        }
        // 该快照日期已经产生过快照数据（快照可能完全成功、部分成功或失败），忽略此快照避免产生重复数据
        SnapshotGuard guard = accountSnapshotDao.findSnapshotGuard(snapshotOn, null);
        if (guard != null) {
            LOG.warn("Snapshot guard already exists, skip this snapshot");
            return;
        }

        // 生成一条快照监视，状态为"快照生成中"
        LocalDateTime now = LocalDateTime.now();
        guard = SnapshotGuard.of(snapshotOn, SnapshotGuard.STATE_PENDING, now);
        // 独立数据库事务保证此记录保存成功
        accountSnapshotDao.insertSnapshotGuard(guard);
        // 查找最近成功的快照记录，作为下次快照的数据基础
        SnapshotGuard lastGuard = accountSnapshotDao.findLastSnapshotGuard(snapshotOn, SnapshotGuard.STATE_DONE);
        // 当本次快照日期前没有成功快照时，设置初始时间为20200101(即20200101至snapshotOn区间的流水数据参与快照计算)
        LocalDate lastSnapshotOn = lastGuard != null ? lastGuard.getSnapshotOn() : LocalDate.of(2020, 01, 01);
        // 快照日期<=20200101时将忽略本次快照，20200101前系统未上线无流水数据
        if (!snapshotOn.isAfter(lastSnapshotOn)) {
            LOG.warn("Cannot take a snapshot on or before last success snapshot, skip this snapshot");
            return;
        }

        // 本次日切数据 = 上次日切数据(lastSnapshotOn) + 实时业务流水delta增量(startOn - endOn)
        Map<String, Object> params = new HashMap<>();
        params.put("lastSnapshotOn", lastSnapshotOn);
        params.put("startOn", lastSnapshotOn.plusDays(1));
        params.put("endOn", snapshotOn.plusDays(1));
        BatchResultHandler resultHandler = new BatchResultHandler(snapshotOn);
        sqlSessionTemplate.select(MAPPER_STATEMENT, params, resultHandler);
        resultHandler.flush();
        guard.success();
        // 独立数据库事务保证此记录更新成功
        accountSnapshotDao.updateSnapshotGuardState(guard);
    }

    private class BatchResultHandler implements ResultHandler<DailySnapshot> {
        // 批量数量
        private static final int BATCH_SIZE = 4000;
        // 批量数据缓存
        private List<DailySnapshot> snapshots = new ArrayList<>(BATCH_SIZE);
        // 快照日期
        private LocalDate snapshotOn;
        // 插入时间
        private LocalDateTime when;

        public BatchResultHandler(LocalDate snapshotOn) {
            this.snapshotOn = snapshotOn;
            this.when = LocalDateTime.now();
        }

        @Override
        public void handleResult(ResultContext<? extends DailySnapshot> resultContext) {
            DailySnapshot snapshot = resultContext.getResultObject();
            snapshot.setSnapshotOn(snapshotOn);
            snapshot.setCreatedTime(when);
            snapshots.add(snapshot);
            if (snapshots.size() >= BATCH_SIZE) {
                flush();
            }
        }

        public void flush() {
            try {
                if (snapshots.size() > 0) {
                    // 独立数据库事务保证批量插入后立即提交数据
                    accountSnapshotDao.batchInsertDailySnapshot(snapshots);
                }
            } finally {
                this.when = LocalDateTime.now();
                snapshots.clear();
            }
        }
    }
}
