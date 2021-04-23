package com.diligrp.xtrade.upay.pipeline;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 支付通道模块SpringBoot集成配置
 */
@Configuration
@ComponentScan("com.diligrp.xtrade.upay.pipeline")
@MapperScan(basePackages =  {"com.diligrp.xtrade.upay.pipeline.dao"}, markerInterface = MybatisMapperSupport.class)
public class PipelineConfiguration {
}