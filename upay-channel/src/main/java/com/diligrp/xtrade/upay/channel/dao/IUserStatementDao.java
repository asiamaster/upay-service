package com.diligrp.xtrade.upay.channel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.channel.domain.UserStatementFilter;
import com.diligrp.xtrade.upay.channel.domain.UserStatementQuery;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.model.UserStatement;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 客户账单数据访问层
 *
 * @author: brenthuang
 * @date: 2020/10/23
 */
@Repository("userStatementDao")
public interface IUserStatementDao extends MybatisMapperSupport {
    /**
     * 添加用户客户账单
     */
    void insertUserStatement(UserStatement statement);

    /**
     * 批量添加客户账单
     */
    void insertUserStatements(List<UserStatement> statements);

    /**
     * 查询客户账单明细
     */
    List<UserStatementDto> listUserStatements(UserStatementQuery query);

    /**
     * 查询客户账单汇总数据：总记录数、总收入和总支出
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