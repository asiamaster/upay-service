package com.diligrp.xtrade.upay.core.util;

import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.exception.PaymentServiceException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步执行工具类
 */
public class AsyncTaskExecutor {
    private static final ExecutorService treadPool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
        Integer.MAX_VALUE, 2, TimeUnit.MINUTES, new LinkedBlockingQueue());

    public static <T> T submit(Callable<T> task) {
        try {
            Future<T> future = treadPool.submit(task);
            return future.get();
        } catch (Exception ex) {
            throw new PaymentServiceException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "异步任务执行异常");
        }
    }

    public static void submit(Runnable task) {
        try {
            Future<?> future = treadPool.submit(task);
            future.get();
        } catch (Exception ex) {
            throw new PaymentServiceException(ErrorCode.SYSTEM_UNKNOWN_ERROR, "异步任务执行异常");
        }
    }
}
