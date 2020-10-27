package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.upay.channel.dao.IUserStatementDao;
import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
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
}
