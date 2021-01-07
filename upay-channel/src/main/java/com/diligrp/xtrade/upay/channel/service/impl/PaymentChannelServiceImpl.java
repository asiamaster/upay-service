package com.diligrp.xtrade.upay.channel.service.impl;

import com.diligrp.xtrade.upay.channel.dao.IPaymentChannelDao;
import com.diligrp.xtrade.upay.channel.domain.BankChannel;
import com.diligrp.xtrade.upay.channel.exception.PaymentChannelException;
import com.diligrp.xtrade.upay.channel.service.IPaymentChannelService;
import com.diligrp.xtrade.upay.channel.type.ChannelType;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IMerchantDao;
import com.diligrp.xtrade.upay.core.domain.MerchantPermit;
import com.diligrp.xtrade.upay.core.service.IAccessPermitService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 支付渠道服务实现
 *
 * @author: brenthuang
 * @date: 2021/01/06
 */
@Service("paymentChannelService")
public class PaymentChannelServiceImpl implements IPaymentChannelService {

    @Resource
    private IMerchantDao merchantDao;

    @Resource
    private IPaymentChannelDao paymentChannelDao;

    @Resource
    private IAccessPermitService accessPermitService;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ChannelType> supportedChannels(Long mchId) {
        MerchantPermit merchant = accessPermitService.loadMerchantPermit(mchId);

        return merchantDao.supportedChannels(merchant.parentMchId()).stream().map(ChannelType::getType)
            .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChannelType bankChannelByCardNo(Long mchId, String bankCardNo) {
        BankChannel channel = paymentChannelDao.findBankChannelByCardNo(bankCardNo).orElseThrow(() ->
            new PaymentChannelException(ErrorCode.OBJECT_NOT_FOUND, "找不到卡号对应的银行渠道"));
        String channelInfo = String.format("%s(%s)", channel.getBankName(), channel.getCode());
        ChannelType channelType = ChannelType.getBankChannel(channel.getCode()).orElseThrow(() ->
            new PaymentChannelException(ErrorCode.OBJECT_NOT_FOUND, "系统不支持此银行渠道:" + channelInfo));

        MerchantPermit merchant = accessPermitService.loadMerchantPermit(mchId);
        merchantDao.supportedChannels(merchant.parentMchId()).stream().filter(type -> type == channelType.getCode()).findAny()
            .orElseThrow(() -> new PaymentChannelException(ErrorCode.OPERATION_NOT_ALLOWED, "该商户不支持此银行渠道:" + channelInfo));
        return channelType;
    }

    /**
     * {@inheritDoc}
     *
     * 根据银行名称模糊查询并获取前20条记录
     */
    @Override
    public List<BankChannel> listBankChannelByName(String bankName) {
        return paymentChannelDao.listBankChannelByName(bankName);
    }
}
