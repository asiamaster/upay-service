package com.diligrp.xtrade.upay.core.service.impl;

import com.diligrp.xtrade.shared.redis.IRedisSystemService;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.shared.util.NumberUtils;
import com.diligrp.xtrade.shared.util.ObjectUtils;
import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.dao.IDataDictionaryDao;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;
import com.diligrp.xtrade.upay.core.model.DataDictionary;
import com.diligrp.xtrade.upay.core.service.IPaymentConfigService;
import com.diligrp.xtrade.upay.core.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 支付服务参数配置服务实现
 */
@Service("paymentConfigService")
public class PaymentConfigServiceImpl implements IPaymentConfigService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private static final String PREFIX_CONFIG_KEY = "upay:dictionary:config:";

    private static final int CACHE_EXPIRE_TIME = 60 * 60;

    @Resource
    private IDataDictionaryDao dataDictionaryDao;

    @Resource
    private IRedisSystemService redisSystemService;

    /**
     * {@inheritDoc}
     *
     * on-开启数据签名验签, off-关闭数据签名验签
     */
    @Override
    public boolean dataSignSwitch(String groupCode) {
        AssertUtils.notEmpty(groupCode, "groupCode missed");
        String cachedKey = PREFIX_CONFIG_KEY + groupCode + Constants.CHAR_UNDERSCORE + Constants.CONFIG_DATA_SIGN;
        String value = loadCachedConfig(cachedKey);
        if (ObjectUtils.isEmpty(value)) {
            DataDictionary config = dataDictionaryDao.findDataDictionaryByCode(Constants.CONFIG_DATA_SIGN, groupCode);
            if (config != null) {
                value = config.getValue();
                saveCachedConfig(cachedKey, value);
            }
        }
        return Constants.SWITCH_ON.equalsIgnoreCase(value);
    }

    /**
     * {@inheritDoc}
     *
     * on-开启短信通知, off-关闭短信通知
     */
    @Override
    public boolean smsNotifySwitch(String groupCode) {
        AssertUtils.notEmpty(groupCode, "groupCode missed");
        String cachedKey = PREFIX_CONFIG_KEY + groupCode + Constants.CHAR_UNDERSCORE + Constants.CONFIG_SMS_NOTIFY;
        String value = loadCachedConfig(cachedKey);
        if (ObjectUtils.isEmpty(value)) {
            DataDictionary config = dataDictionaryDao.findDataDictionaryByCode(Constants.CONFIG_SMS_NOTIFY, groupCode);
            if (config != null) {
                value = config.getValue();
                saveCachedConfig(cachedKey, value);
            }
        }
        return Constants.SWITCH_ON.equalsIgnoreCase(value);
    }

    /**
     * {@inheritDoc}
     *
     * 设置最大免密支付金额
     */
    @Override
    public Long maxProtocolAmount(String groupCode, Integer protocolType) {
        AssertUtils.notEmpty(groupCode, "groupCode missed");
        String cachedKey = PREFIX_CONFIG_KEY + groupCode + Constants.CHAR_UNDERSCORE
            + Constants.CONFIG_MAX_PROTO_AMOUNT + protocolType;
        String value = loadCachedConfig(cachedKey);
        if (ObjectUtils.isEmpty(value)) {
            DataDictionary config = dataDictionaryDao.findDataDictionaryByCode(Constants.CONFIG_MAX_PROTO_AMOUNT
                + protocolType, groupCode);
            if (config != null) {
                value = config.getValue();
                saveCachedConfig(cachedKey, value);
            }
        }
        // 如无配置信息则使用全局配置金额100元 @see Constants.DEFAULT_MAX_PROTO_AMOUNT
        value = ObjectUtils.isEmpty(value) ? Constants.DEFAULT_MAX_PROTO_AMOUNT : value;
        if (!NumberUtils.isNumeric(value)) {
            throw new PaymentServiceException(ErrorCode.ILLEGAL_ARGUMENT_ERROR, "免密协议参数配置错误");
        }
        return Long.parseLong(value);
    }

    /**
     * {@inheritDoc}
     *
     * 全局参数配置使用默认分组@see Constants.GLOBAL_CFG_GROUP
     */
    @Override
    public String loadSystemGlobalConf(String code) {
        AssertUtils.notEmpty(code, "code missed");
        String cachedKey = PREFIX_CONFIG_KEY + Constants.GLOBAL_CFG_GROUP + Constants.CHAR_UNDERSCORE + code;
        String value = loadCachedConfig(cachedKey);
        if (ObjectUtils.isEmpty(value)) {
            DataDictionary config = dataDictionaryDao.findDataDictionaryByCode(code, Constants.GLOBAL_CFG_GROUP);
            if (config != null) {
                value = config.getValue();
                saveCachedConfig(cachedKey, value);
            }
        }
        return value;
    }

    /**
     * Redis加载缓存的数据字典配置，程序异常时返回NULL
     */
    private String loadCachedConfig(String cachedKey) {
        try {
            return redisSystemService.getAndExpire(cachedKey, CACHE_EXPIRE_TIME);
        } catch (Exception ex) {
            LOG.error("Failed to load cached config", ex);
        }
        return null;
    }

    /**
     * Redis缓存存储数据字典配置，忽略程序异常
     */
    private void saveCachedConfig(String cachedKey, String cachedValue) {
        try {
            redisSystemService.setAndExpire(cachedKey, cachedValue, CACHE_EXPIRE_TIME);
        } catch (Exception ex) {
            LOG.error("Failed to save cached config", ex);
        }
    }
}
