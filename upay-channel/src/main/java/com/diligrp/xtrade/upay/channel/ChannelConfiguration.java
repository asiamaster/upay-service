package com.diligrp.xtrade.upay.channel;

import com.diligrp.xtrade.shared.mybatis.MybatisMapperSupport;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 渠道模块SpringBoot集成配置
 */
@Configuration
@ComponentScan("com.diligrp.xtrade.upay.channel")
@MapperScan(basePackages =  {"com.diligrp.xtrade.upay.channel.dao"}, markerInterface = MybatisMapperSupport.class)
public class ChannelConfiguration {
}