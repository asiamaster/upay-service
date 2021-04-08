package com.diligrp.xtrade.upay.sentinel.service.impl;

import com.diligrp.xtrade.shared.redis.IRedisSystemService;
import com.diligrp.xtrade.shared.util.DateUtils;
import com.diligrp.xtrade.shared.util.NumberUtils;
import com.diligrp.xtrade.upay.sentinel.domain.ExecuteContext;
import com.diligrp.xtrade.upay.sentinel.domain.Passport;
import com.diligrp.xtrade.upay.sentinel.service.ISentinelAssistant;
import com.diligrp.xtrade.upay.sentinel.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Pipeline;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 默认风控引擎执行助手
 *
 * @author: brenthuang
 * @date: 2021/03/08
 */
@Service("sentinelAssistant")
public class DefaultSentinelAssistant implements ISentinelAssistant {

    private static Logger LOG = LoggerFactory.getLogger(DefaultSentinelAssistant.class);

    @Resource
    private IRedisSystemService redisSystemService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecuteContext loadWithdrawExecuteContext(Passport passport) {
        ExecuteContext context = new ExecuteContext();
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, DateUtils.YYYYMMDD);
        String month = DateUtils.formatDate(now, Constants.YYYYMM);

        IRedisSystemService.IConnectionCallback connectionCallback = connection -> {
            Pipeline pipeline = connection.pipelined();
            // upay:sentinel:withdraw:{accountId}:{today}:dailyAmount
            String dailyAmountKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_DAILYAMOUNT);
            pipeline.get(dailyAmountKey);
            // upay:sentinel:withdraw:{accountId}:{today}:dailyTimes
            String dailyTimesKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_DAILYTIMES);
            pipeline.get(dailyTimesKey);
            // upay:sentinel:withdraw:{accountId}:{month}:monthlyAmount
            String monthlyAmountKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(month).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_MONTHLYAMOUNT);
            pipeline.get(monthlyAmountKey);
            // 使用pipeline模式一次交互获取所需值，优化访问性能
            int dailyTimes = 0;
            long dailyAmount = 0, monthlyAmount = 0;
            List<Object> result = pipeline.syncAndReturnAll();
            if (result != null && result.size() == 3) {
                dailyAmount = NumberUtils.str2Long(result.get(0).toString(), 0);
                dailyTimes = NumberUtils.str2Int(result.get(1).toString(), 0);
                monthlyAmount = NumberUtils.str2Long(result.get(2).toString(), 0);
            }
            context.setDailyAmount(dailyAmount);
            context.setDailyTimes(dailyTimes);
            context.setMonthlyAmount(monthlyAmount);
        };

        try {
            redisSystemService.execute(connectionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: load withdraw execute context from redis error", ex);
        }

        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshWithdrawExecuteContext(Passport passport) {
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, DateUtils.YYYYMMDD);
        String thisMonth = DateUtils.formatDate(now, Constants.YYYYMM);
        String yesterday = DateUtils.formatDate(now.minusDays(1), DateUtils.YYYYMMDD);
        String lastMonth = DateUtils.formatDate(now.minusMonths(1), Constants.YYYYMM);

        IRedisSystemService.IConnectionCallback connectionCallback = connection -> {
            Pipeline pipeline = connection.pipelined();
            // upay:sentinel:withdraw:{accountId}:{today}:dailyAmount
            String dailyAmountKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_DAILYAMOUNT);
            pipeline.incrBy(dailyAmountKey, passport.getAmount());
            pipeline.expire(dailyAmountKey, Constants.ONE_DAY_SECONDS);
            // upay:sentinel:withdraw:{accountId}:{today}:dailyTimes
            String dailyTimesKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_DAILYTIMES);
            pipeline.incrBy(dailyTimesKey, 1);
            pipeline.expire(dailyTimesKey, Constants.ONE_DAY_SECONDS);
            // 获取当月最后一天，并计算间隔天数，将间隔天数+1作为月提现金额的过期时间
            LocalDate lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
            int days = (int)now.until(lastDay, ChronoUnit.DAYS) + 1;
            // upay:sentinel:withdraw:{accountId}:{month}:monthlyAmount
            String monthlyAmountKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(thisMonth).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_MONTHLYAMOUNT);
            pipeline.incrBy(monthlyAmountKey, passport.getAmount());
            pipeline.expire(monthlyAmountKey, Constants.ONE_DAY_SECONDS * days);
            // 清理上个时间周期的缓存值，优化Redis存储
            String lastDailyAmountKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(yesterday).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_DAILYAMOUNT);
            pipeline.del(lastDailyAmountKey);
            String lastDailyTimesKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(yesterday).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_DAILYTIMES);
            pipeline.del(lastDailyTimesKey);
            String lastMonthlyAmountKey = Constants.SENTINEL_WITHDRAW_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(lastMonth).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_WITHDRAW_MONTHLYAMOUNT);
            pipeline.del(lastMonthlyAmountKey);

            pipeline.sync();
            LOG.debug("风控提示-资金账号: {} 提现金额: {}", passport.getAccountId(), passport.getAmount());
        };

        try {
            redisSystemService.execute(connectionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: refresh withdraw execute context into redis error", ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExecuteContext loadTradeExecuteContext(Passport passport) {
        ExecuteContext context = new ExecuteContext();
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, DateUtils.YYYYMMDD);
        String month = DateUtils.formatDate(now, Constants.YYYYMM);

        IRedisSystemService.IConnectionCallback connectionCallback = connection -> {
            Pipeline pipeline = connection.pipelined();
            // upay:sentinel:trade:{accountId}:{today}:dailyAmount
            String dailyAmountKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_DAILYAMOUNT);
            pipeline.get(dailyAmountKey);
            // upay:sentinel:trade:{accountId}:{today}:dailyTimes
            String dailyTimesKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_DAILYTIMES);
            pipeline.get(dailyTimesKey);
            // upay:sentinel:trade:{accountId}:{month}:monthlyAmount
            String monthlyAmountKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(month).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_MONTHLYAMOUNT);
            pipeline.get(monthlyAmountKey);
            // 使用pipeline模式一次交互获取所需值，优化访问性能
            int dailyTimes = 0;
            long dailyAmount = 0, monthlyAmount = 0;
            List<Object> result = pipeline.syncAndReturnAll();
            if (result != null && result.size() == 3) {
                dailyAmount = NumberUtils.str2Long(result.get(0).toString(), 0);
                dailyTimes = NumberUtils.str2Int(result.get(1).toString(), 0);
                monthlyAmount = NumberUtils.str2Long(result.get(2).toString(), 0);
            }
            context.setDailyAmount(dailyAmount);
            context.setDailyTimes(dailyTimes);
            context.setMonthlyAmount(monthlyAmount);
        };

        try {
            redisSystemService.execute(connectionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: load trade execute context from redis error", ex);
        }

        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshTradeExecuteContext(Passport passport) {
        LocalDate now = LocalDate.now();
        String today = DateUtils.formatDate(now, DateUtils.YYYYMMDD);
        String thisMonth = DateUtils.formatDate(now, Constants.YYYYMM);
        String yesterday = DateUtils.formatDate(now.minusDays(1), DateUtils.YYYYMMDD);
        String lastMonth = DateUtils.formatDate(now.minusMonths(1), Constants.YYYYMM);

        IRedisSystemService.IConnectionCallback connectionCallback = connection -> {
            Pipeline pipeline = connection.pipelined();
            // upay:sentinel:trade:{accountId}:{today}:dailyAmount
            String dailyAmountKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_DAILYAMOUNT);
            pipeline.incrBy(dailyAmountKey, passport.getAmount());
            pipeline.expire(dailyAmountKey, Constants.ONE_DAY_SECONDS);
            // upay:sentinel:trade:{accountId}:{today}:dailyTimes
            String dailyTimesKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(today).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_DAILYTIMES);
            pipeline.incrBy(dailyTimesKey, 1);
            pipeline.expire(dailyTimesKey, Constants.ONE_DAY_SECONDS);
            // 获取当月最后一天，并计算间隔天数，将间隔天数+1作为月提现金额的过期时间
            LocalDate lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
            int days = (int)now.until(lastDay, ChronoUnit.DAYS) + 1;
            // upay:sentinel:trade:{accountId}:{month}:monthlyAmount
            String monthlyAmountKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(thisMonth).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_MONTHLYAMOUNT);
            pipeline.incrBy(monthlyAmountKey, passport.getAmount());
            pipeline.expire(monthlyAmountKey, Constants.ONE_DAY_SECONDS * days);
            // 清理上个时间周期的缓存值，优化Redis存储
            String lastDailyAmountKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(yesterday).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_DAILYAMOUNT);
            pipeline.del(lastDailyAmountKey);
            String lastDailyTimesKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(yesterday).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_DAILYTIMES);
            pipeline.del(lastDailyTimesKey);
            String lastMonthlyAmountKey = Constants.SENTINEL_TRADE_PREFIX.concat(String.valueOf(passport.getAccountId()))
                .concat(Constants.CHAR_COLON).concat(lastMonth).concat(Constants.CHAR_COLON)
                .concat(Constants.SENTINEL_TRADE_MONTHLYAMOUNT);
            pipeline.del(lastMonthlyAmountKey);

            pipeline.sync();
            LOG.debug("风控提示-资金账号: {} 发生交易额: {}", passport.getAccountId(), passport.getAmount());
        };

        try {
            redisSystemService.execute(connectionCallback);
        } catch (Exception ex) {
            LOG.error("RiskControl: refresh trade execute context into redis error", ex);
        }
    }
}
