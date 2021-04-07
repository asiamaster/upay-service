package com.diligrp.xtrade.upay.channel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.channel.domain.UserStatementFilter;
import com.diligrp.xtrade.upay.channel.domain.UserStatementQuery;
import com.diligrp.xtrade.upay.channel.domain.UserStatementDto;
import com.diligrp.xtrade.upay.channel.domain.SumUserStatement;
import com.diligrp.xtrade.upay.channel.model.UserStatement;
import com.diligrp.xtrade.upay.core.util.DataPartition;
import org.apache.ibatis.annotations.Param;
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
    void insertUserStatement(@Param("strategy") DataPartition strategy, @Param("statement") UserStatement statement);

    /**
     * 批量添加客户账单
     */
    void insertUserStatements(@Param("strategy") DataPartition strategy, @Param("statements") List<UserStatement> statements);

    /**
     * 查询客户账单明细
     */
    List<UserStatementDto> listUserStatements(@Param("strategy") DataPartition strategy, @Param("query") UserStatementQuery query);

    /**
     * 查询客户账单汇总数据：总记录数、总收入和总支出
     */
    SumUserStatement sumUserStatements(@Param("strategy") DataPartition strategy, @Param("query") UserStatementQuery query);

    /**
     * 根据交易号和账号ID查询客户交易（非"退款"）账单
     */
    UserStatementDto findUserStatement(@Param("strategy") DataPartition strategy, @Param("filter") UserStatementFilter filter);

    /**
     * 查询客户退款账单
     */
    List<UserStatementDto> listRefundStatements(@Param("strategy") DataPartition strategy, @Param("filter") UserStatementFilter filter);
}