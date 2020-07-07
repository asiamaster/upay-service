package com.diligrp.xtrade.upay.core;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 核心模块SpringBoot集成配置
 */
@Configuration
@ComponentScan("com.diligrp.xtrade.upay.core")
@MapperScan(basePackages =  {"com.diligrp.xtrade.upay.core.dao"}, markerInterface = MybatisMapperSupport.class)
public class CoreConfiguration {
}