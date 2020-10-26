package com.diligrp.xtrade.upay.channel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.channel.domain.TradeQuery;
import com.diligrp.xtrade.upay.channel.domain.TradeStatement;
import com.diligrp.xtrade.upay.channel.domain.SumTradeStatement;
import com.diligrp.xtrade.upay.channel.model.UserStatement;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 用户业务账单数据访问层
 *
 * @author: brenthuang
 * @date: 2020/10/23
 */
@Repository("userStatementDao")
public interface IUserStatementDao extends MybatisMapperSupport {
    /**
     * 添加用户业务账单
     */
    void insertUserStatement(UserStatement statement);

    /**
     * 批量添加用户业务账单
     */
    void insertUserStatements(List<UserStatement> statements);

    /**
     * 查询客户交易明细
     */
    List<TradeStatement> listTradeStatements(TradeQuery query);

    /**
     * 查询客户交易总记录数
     */
    long countTradeStatements(TradeQuery query);

    /**
     * 查询客户总收入和总支出
     */
    SumTradeStatement sumTradeStatements(TradeQuery query);
}