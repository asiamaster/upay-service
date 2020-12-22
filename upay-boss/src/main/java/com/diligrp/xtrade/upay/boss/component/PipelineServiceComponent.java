package com.diligrp.xtrade.upay.boss.component;

import com.diligrp.xtrade.shared.domain.PageMessage;
import com.diligrp.xtrade.shared.domain.ServiceRequest;
import com.diligrp.xtrade.shared.sapi.CallableComponent;
import com.diligrp.xtrade.shared.util.AssertUtils;
import com.diligrp.xtrade.upay.core.domain.ApplicationPermit;
import com.diligrp.xtrade.upay.pipeline.domain.PipelineStatementQuery;
import com.diligrp.xtrade.upay.pipeline.domain.UserPipelineStatement;
import com.diligrp.xtrade.upay.trade.service.IPipelinePaymentProcessor;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * 支付通道服务组件
 */
@CallableComponent(id = "payment.pipeline.service")
public class PipelineServiceComponent {

    @Resource
    private IPipelinePaymentProcessor pipelinePaymentProcessor;

    /**
     * 分页查询当日客户通道支付记录
     */
    public PageMessage<UserPipelineStatement> list(ServiceRequest<PipelineStatementQuery> request) {
        PipelineStatementQuery query = request.getData();
        AssertUtils.notNull(query.getType(), "type missed");
        AssertUtils.notNull(query.getStartDate(), "startDate missed");
        AssertUtils.notNull(query.getPageNo(), "pageNo missed");
        AssertUtils.notNull(query.getPageSize(), "pageSize missed");
        AssertUtils.isTrue(query.getPageNo() > 0, "invalid pageNo");
        AssertUtils.isTrue(query.getPageSize() > 0, "invalid pageSize");
        LocalDate endDate = query.getEndDate() != null ? query.getEndDate().plusDays(1) : query.getEndDate();
        query.setEndDate(endDate);
        query.from(query.getPageNo(), query.getPageSize());

        ApplicationPermit permit = request.getContext().getObject(ApplicationPermit.class.getName(), ApplicationPermit.class);
        query.setMchId(permit.getMerchant().getMchId());
        return pipelinePaymentProcessor.listPipelineStatements(query);
    }
}
