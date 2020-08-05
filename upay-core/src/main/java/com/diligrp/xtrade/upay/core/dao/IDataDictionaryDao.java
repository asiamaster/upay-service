package com.diligrp.xtrade.upay.core.dao;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import com.diligrp.xtrade.upay.core.model.DataDictionary;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 账户资金数据访问层
 *
 * 数据字典设计：参数配置唯一约束：code + group_code, 允许出现重复code的配置参数；但不允许
 * 在某个参数分组中(group_code)重复出现code的配置参数，这样设计的目的是允许不同商户下某个参数
 * 配置有不同的值; 根据业务场景使用不同的数据字典API（返回一条还是多条字典配置）
 */
@Repository("dataDictionaryDao")
public interface IDataDictionaryDao extends MybatisMapperSupport {
    /**
     * 根据编码查询数据字典配置 - 查询结果大于一条记录将抛出异常
     *
     * @param code - 参数编码，必填
     * @param groupCode - 分组编码，非必填
     * @return DataDictionary - 查询结果大于一条记录将抛出异常
     */
    DataDictionary findDataDictionaryByCode(@Param("code") String code, @Param("groupCode") String groupCode);

    /**
     * 根据编码查询数据字典列表
     *
     * @param code - 参数编码，必填
     * @param groupCode - 分组编码，非必填
     * @return List<DataDictionary> - 数据字典列表
     */
    List<DataDictionary> findDataDictionaries(@Param("code") String code, @Param("groupCode") String groupCode);

    /**
     * 根据ID更新数据字典参数值
     */
    int updateDataDictionaryById(@Param("id") Long id, @Param("value") String value);
}
