package com.diligrp.xtrade.upay.channel.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.channel.domain.BankChannel;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 支付通道数据访问层
 *
 * @author: brenthuang
 * @date: 2020/10/23
 */
@Repository("paymentChannelDao")
public interface IPaymentChannelDao extends MybatisMapperSupport {

    /**
     * 根据银行卡号获取银行渠道信息
     */
    Optional<BankChannel> findBankChannelByCardNo(String bankCardNo);

    /**
     * 根据银行名称模糊查询开户行列表
     */
    List<BankChannel> listBankChannelByName(String bankName);
}