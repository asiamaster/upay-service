package com.diligrp.xtrade.upay.core.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.core.model.FundAccount;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 账户资金数据访问层
 */
@Repository("fundAccountDao")
public interface IFundAccountDao extends MybatisMapperSupport {
    void insertFundAccount(FundAccount fund);

    Optional<FundAccount> findFundAccountById(Long accountId);

    Optional<FundAccount> lockFundAccountById(Long accountId);

    int updateFundAccountById(FundAccount fundAccount);

    int compareAndSetVersion(FundAccount fundAccount);

    Optional<FundAccount> sumCustomerFund(Long customerId);

    List<FundAccount> listFundAccounts(Long customerId);
}
