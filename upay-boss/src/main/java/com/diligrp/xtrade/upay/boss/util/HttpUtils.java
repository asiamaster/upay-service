package com.diligrp.xtrade.upay.boss.util;

import com.diligrp.xtrade.shared.domain.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Enumeration;

/**
 * HTTP工具类
 */
public final class HttpUtils {

    private static Logger LOG = LoggerFactory.getLogger(HttpUtils.class);

    public static String httpBody(HttpServletRequest request) {
        StringBuilder payload = new StringBuilder();
        try {
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                payload.append(line);
            }
        } catch (IOException iex) {
            LOG.error("Failed to extract http body", iex);
        }

        return payload.toString();
    }

    public static final void sendResponse(HttpServletResponse response, String payload) {
        try {
            response.setContentType(Constants.CONTENT_TYPE);
            byte[] responseBytes = payload.getBytes(Constants.CHARSET_UTF8);
            response.setContentLength(responseBytes.length);
            response.getOutputStream().write(responseBytes);
            response.flushBuffer();
        } catch (IOException iex) {
            LOG.error("Failed to write data packet back");
        }
    }

    public static RequestContext requestContext(HttpServletRequest request) {
        RequestContext context = new RequestContext();
        Enumeration<String> headers = request.getHeaderNames();
        while (headers.hasMoreElements()) {
            String name = headers.nextElement();
            String value = request.getHeader(name);
            context.put(name, value);
        }

        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            String value = request.getParameter(name);
            context.put(name, value);
        }
        return context;
    }
}
