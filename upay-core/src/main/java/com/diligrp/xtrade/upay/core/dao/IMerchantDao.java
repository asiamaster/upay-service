package com.diligrp.xtrade.upay.core.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.core.model.Application;
import com.diligrp.xtrade.upay.core.model.Merchant;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商户/应用数据访问层
 */
@Repository("merchantDao")
public interface IMerchantDao extends MybatisMapperSupport {
    Optional<Application> findApplicationById(Long appId);

    Optional<Merchant> findMerchantById(Long mchId);

    void insertMerchant(Merchant merchant);

    void insertApplication(Application application);
}
