package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.upay.channel.dao.IUserStatementDao;
import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.channel.domain.UserStatementFilter;
import com.diligrp.xtrade.upay.channel.domain.UserStatementQuery;
import com.diligrp.xtrade.upay.channel.service.IUserStatementService;
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
    public List<UserStatementDto> listUserStatements(UserStatementQuery query) {
        return userStatementDao.listUserStatements(query);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SumUserStatement sumUserStatements(UserStatementQuery query) {
        return userStatementDao.sumUserStatements(query);
    }

    /**
     * {@inheritDoc}
     *
     * 某个资金账号的某笔交易有且只有一条(非退款的)交易账单
     */
    @Override
    public UserStatementDto findUserStatement(UserStatementFilter filter) {
        return userStatementDao.findUserStatement(filter);
    }

    /**
     * {@inheritDoc}
     *
     * 某个资金账号的某笔交易有一条或多条退款账单
     */
    @Override
    public List<UserStatementDto> listRefundStatements(UserStatementFilter filter) {
        return userStatementDao.listRefundStatements(filter);
    }
}
