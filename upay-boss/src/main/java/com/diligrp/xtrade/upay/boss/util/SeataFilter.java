package com.diligrp.xtrade.upay.boss.util;

import io.seata.core.context.RootContext;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 支付作为底层服务，目前不会主动发起全局事务，因此只需被动处理其他服务RPC传递过来的全局事务XID
 * 如果后期需要主动发起全局事务，则需要添加RestTemplate和Feign等的拦截器，主动传递全局事务XID
 * 目前未集成Seata Spring Cloud，原因最新Cloud版本只支持seata 1.1.0，且支付服务目前只被动全局事务使用较为简单
 */
@Component
public class SeataFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest httpRequest, ServletResponse httpResponse, FilterChain filterChain)
        throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) httpRequest;
        String xid = request.getHeader(RootContext.KEY_XID);

        boolean isBind = false;
        if (xid != null) {
            RootContext.bind(xid);
            isBind = true;
        }

        try {
            filterChain.doFilter(httpRequest, httpResponse);
        } finally {
            if (isBind) {
                RootContext.unbind();
            }
        }
    }

    @Override
    public void destroy() {
    }
}
