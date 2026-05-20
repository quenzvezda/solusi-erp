package com.solusi.erp.common.approval.infrastructure.adapter;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.core.event.ApprovalRejectedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter to publish events from Approval module using Spring ApplicationEventPublisher.
 */
@Component
@RequiredArgsConstructor
public class ApprovalEventPublisherAdapter implements ApprovalEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishCompleted(String referenceType, Long referenceId) {
        eventPublisher.publishEvent(new ApprovalCompletedEvent(referenceType, referenceId));
    }

    @Override
    public void publishRejected(String referenceType, Long referenceId) {
        eventPublisher.publishEvent(new ApprovalRejectedEvent(referenceType, referenceId));
    }
}
