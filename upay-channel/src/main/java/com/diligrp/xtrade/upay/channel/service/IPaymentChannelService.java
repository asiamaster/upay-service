package com.diligrp.xtrade.upay.channel.service;

import com.diligrp.xtrade.upay.channel.domain.BankChannel;
import com.diligrp.xtrade.upay.channel.type.ChannelType;

import java.util.List;

/**
 * 支付渠道服务接口
 *
 * @author: brenthuang
 * @date: 2021/01/06
 */
public interface IPaymentChannelService {

    /**
     * 查询商户支持的支付渠道
     *
     * @param mchId - 商户ID
     * @return 支付渠道列表
     */
    List<ChannelType> supportedChannels(Long mchId);

    /**
     * 根据银行卡号获取该商户允许的支付渠道
     * 如果银行卡对应的银行渠道不在商户支付渠道列表中则抛出异常
     *
     * @param mchId - 商户ID
     * @param bankCardNo - 银行卡号
     * @return 支付渠道
     */
    ChannelType bankChannelByCardNo(Long mchId, String bankCardNo);

    /**
     * 根据银行名称模糊查询开户行列表
     *
     * @param bankName - 银行名称
     * @return 开户行列表
     */
    List<BankChannel> listBankChannelByName(String bankName);
}
