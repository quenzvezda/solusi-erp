package com.solusi.erp.common.approval.infrastructure.adapter;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.common.approval.application.service.ApprovalActionOccurredEventFactory;
import com.solusi.erp.common.approval.domain.model.ApprovalAction;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.core.event.ApprovalCompletedEvent;
import com.solusi.erp.core.event.ApprovalRejectedEvent;
import com.solusi.erp.core.messaging.application.port.IntegrationEventPublisher;
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
    private final IntegrationEventPublisher integrationEventPublisher;
    private final ApprovalActionOccurredEventFactory approvalActionOccurredEventFactory;

    @Override
    public void publishRequested(ApprovalRequest request) {
        publishIntegrationEvent(request, ApprovalAction.REQUESTED);
    }

    @Override
    public void publishCompleted(ApprovalRequest request, Long actorId) {
        eventPublisher.publishEvent(new ApprovalCompletedEvent(
                request.getReferenceType(), request.getReferenceId(), actorId));
        publishIntegrationEvent(request, ApprovalAction.APPROVE_AND_FINISH);
    }

    @Override
    public void publishRejected(ApprovalRequest request, Long actorId) {
        eventPublisher.publishEvent(new ApprovalRejectedEvent(request.getReferenceType(), request.getReferenceId()));
        publishIntegrationEvent(request, ApprovalAction.REJECTED);
    }

    @Override
    public void publishForwarded(ApprovalRequest request, Long actorId, Long targetApproverId) {
        publishIntegrationEvent(request, ApprovalAction.FORWARD);
    }

    @Override
    public void publishApprovedAndForwarded(ApprovalRequest request, Long actorId, Long targetApproverId) {
        publishIntegrationEvent(request, ApprovalAction.APPROVE_AND_FORWARD);
    }

    private void publishIntegrationEvent(ApprovalRequest request, ApprovalAction action) {
        integrationEventPublisher.publish(approvalActionOccurredEventFactory.create(request, action));
    }
}
