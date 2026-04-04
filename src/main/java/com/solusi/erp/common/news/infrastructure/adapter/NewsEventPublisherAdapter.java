package com.solusi.erp.common.news.infrastructure.adapter;

import com.solusi.erp.common.news.application.port.NewsEventPublisher;
import com.solusi.erp.core.event.ApprovalRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Infrastructure Adapter for Publishing Events via Spring ApplicationEventPublisher.
 */
@Component
@RequiredArgsConstructor
public class NewsEventPublisherAdapter implements NewsEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishApprovalRequested(Long newsId, String requester, Long approverId) {
        eventPublisher.publishEvent(new ApprovalRequestedEvent("NEWS", newsId, requester, approverId));
    }
}
