package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.channel.domain.UserStatementQuery;

import java.util.List;

/**
 * 渠道流水服务接口
 *
 * @author: brenthuang
 * @date: 2020/10/14
 */
public interface IUserStatementService {
    /**
     * 分页查询客户交易明细
     */
    List<UserStatementDto> listUserStatements(UserStatementQuery query);

    /**
     * 查询客户总收入和总支出
     */
    SumUserStatement sumUserStatements(UserStatementQuery query);
}
