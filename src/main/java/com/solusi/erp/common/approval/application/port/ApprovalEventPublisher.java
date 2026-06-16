package com.solusi.erp.common.approval.application.port;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;

/**
 * Port for publishing events from the Approval Module.
 */
public interface ApprovalEventPublisher {
    void publishRequested(ApprovalRequest request);

    void publishCompleted(ApprovalRequest request, Long actorId);

    void publishRejected(ApprovalRequest request, Long actorId);

    void publishForwarded(ApprovalRequest request, Long actorId, Long targetApproverId);

    void publishApprovedAndForwarded(ApprovalRequest request, Long actorId, Long targetApproverId);
}
