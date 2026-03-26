package com.solusi.erp.common.approval.application.port;

/**
 * Port for publishing events from the Approval Module.
 */
public interface ApprovalEventPublisher {
    void publishCompleted(String referenceType, Long referenceId);
    void publishRejected(String referenceType, Long referenceId);
}
