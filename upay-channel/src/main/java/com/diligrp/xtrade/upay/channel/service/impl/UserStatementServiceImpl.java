package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.upay.channel.dao.IUserStatementDao;
import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.channel.domain.UserStatementFilter;
import com.diligrp.xtrade.upay.channel.domain.UserStatementQuery;
import com.diligrp.xtrade.upay.channel.service.IUserStatementService;
import com.diligrp.xtrade.upay.core.util.DataPartition;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 客户账单服务接口
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
@Service("userStatementService")
public class UserStatementServiceImpl implements IUserStatementService {

    @Resource
    private IUserStatementDao userStatementDao;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserStatementDto> listUserStatements(Long mchId, UserStatementQuery query) {
        return userStatementDao.listUserStatements(DataPartition.strategy(mchId), query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SumUserStatement sumUserStatements(Long mchId, UserStatementQuery query) {
        return userStatementDao.sumUserStatements(DataPartition.strategy(mchId), query);
    }

    /**
     * {@inheritDoc}
     *
     * 某个资金账号的某笔交易有且只有一条(非退款的)交易账单
     */
    @Override
    public UserStatementDto findUserStatement(Long mchId, UserStatementFilter filter) {
        return userStatementDao.findUserStatement(DataPartition.strategy(mchId), filter);
    }

    /**
     * {@inheritDoc}
     *
     * 某个资金账号的某笔交易有一条或多条退款账单
     */
    @Override
    public List<UserStatementDto> listRefundStatements(Long mchId, UserStatementFilter filter) {
        return userStatementDao.listRefundStatements(DataPartition.strategy(mchId), filter);
    }
}
