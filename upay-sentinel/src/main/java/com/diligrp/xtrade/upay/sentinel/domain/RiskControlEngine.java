package com.diligrp.xtrade.upay.sentinel.domain;

import com.diligrp.xtrade.upay.core.ErrorCode;
import com.diligrp.xtrade.upay.core.util.AsyncTaskExecutor;
import com.diligrp.xtrade.upay.sentinel.exception.RiskControlException;
import com.diligrp.xtrade.upay.sentinel.service.IExecuteAssistant;
import com.diligrp.xtrade.upay.sentinel.type.PassportType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 风险控制领域模型
 *
 * @author: brenthuang
 * @date: 2021/03/01
 */
public class RiskControlEngine {

    private static Logger LOG = LoggerFactory.getLogger(RiskControlEngine.class);

    private RiskControlContext context;

    public void initEngine(IExecuteAssistant executeAssistant) {
        this.context = new RiskControlContext();
        this.context.configContext(executeAssistant);
    }

    public RiskControlContext context() {
        if (this.context == null) {
            throw new RiskControlException(ErrorCode.OPERATION_NOT_ALLOWED, "商户未启用风控功能");
        }

        return this.context;
    }

    public void forDeposit(DepositSentinel deposit) {
        context().forDeposit(deposit);
    }

    public void forWithdraw(WithdrawSentinel withdraw) {
        context().forWithdraw(withdraw);
    }

    public void forTrade(TradeSentinel trade) {
        context().forTrade(trade);
    }

    public void checkPassport(Passport passport) {
        if (context != null) {
            if (passport.getType() == PassportType.FOR_DEPOSIT) {
                context.forDeposit().checkPassport(passport);
            } else if (passport.getType() == PassportType.FOR_WITHDRAW) {
                context.forWithdraw().checkPassport(passport);
            } else if (passport.getType() == PassportType.FOR_TRADE) {
                context.forTrade().checkPassport(passport);
            }
        }
    }

    public void admitPassport(Passport passport) {
        if (context != null) {
            // 异步执行, 以便提升程序性能
            AsyncTaskExecutor.submit(() -> {
                try {
                    if (passport.getType() == PassportType.FOR_DEPOSIT) {
                        context.forDeposit().admitPassport(passport);
                    } else if (passport.getType() == PassportType.FOR_WITHDRAW) {
                        context.forWithdraw().admitPassport(passport);
                    } else if (passport.getType() == PassportType.FOR_TRADE) {
                        context.forTrade().admitPassport(passport);
                    }
                } catch (Exception ex) {
                    LOG.error("RiskControl: execute admitPassport error", ex);
                }
            });
        }
    }
}
