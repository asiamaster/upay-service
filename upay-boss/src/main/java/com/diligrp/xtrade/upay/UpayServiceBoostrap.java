package com.diligrp.xtrade.upay;

import com.diligrp.xtrade.upay.boss.BossConfiguration;
import com.diligrp.xtrade.upay.channel.ChannelConfiguration;
import com.diligrp.xtrade.upay.core.CoreConfiguration;
import com.diligrp.xtrade.upay.trade.TradeConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 支付服务启动入口类
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackageClasses = {BossConfiguration.class, TradeConfiguration.class,
        ChannelConfiguration.class, CoreConfiguration.class})
@EnableDiscoveryClient
public class UpayServiceBoostrap {
    public static void main(String[] args) {
        SpringApplication.run(UpayServiceBoostrap.class, args);
    }
}