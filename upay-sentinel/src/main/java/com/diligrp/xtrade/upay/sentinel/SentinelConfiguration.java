package com.diligrp.xtrade.upay.sentinel;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 哨兵模块SpringBoot集成配置
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
@Configuration
@ComponentScan("com.diligrp.xtrade.upay.sentinel")
@MapperScan(basePackages =  {"com.diligrp.xtrade.upay.sentinel.dao"}, markerInterface = MybatisMapperSupport.class)
public class SentinelConfiguration {
}
