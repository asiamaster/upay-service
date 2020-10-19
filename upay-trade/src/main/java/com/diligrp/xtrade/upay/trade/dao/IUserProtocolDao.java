package com.diligrp.xtrade.upay.trade.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.trade.model.UserProtocol;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 免密支付协议数据访问层
 *
 * @author: brenthuang
 * @date: 2020/10/10
 */
@Repository("userProtocolDao")
public interface IUserProtocolDao extends MybatisMapperSupport {
    void insertUserProtocol(UserProtocol protocol);

    Optional<UserProtocol> findUserProtocol(@Param("accountId") Long accountId, @Param("type") Integer type);

    Optional<UserProtocol> findUserProtocolById(Long protocolId);

    int compareAndSetState(UserProtocol protocol);
}
