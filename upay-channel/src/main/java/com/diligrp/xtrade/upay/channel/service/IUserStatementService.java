package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.channel.domain.UserStatementFilter;
import com.diligrp.xtrade.upay.channel.domain.UserStatementQuery;
import com.diligrp.xtrade.upay.channel.model.UserStatement;

import java.util.List;

/**
 * 客户账单服务接口
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

    /**
     * 根据交易号和账号ID查询客户交易（非"退款"）账单
     */
    UserStatementDto findUserStatement(UserStatementFilter filter);

    /**
     * 查询客户退款账单
     */
    List<UserStatementDto> listRefundStatements(UserStatementFilter filter);
}
