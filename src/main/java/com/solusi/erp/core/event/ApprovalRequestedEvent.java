package com.solusi.erp.core.event;

import lombok.Getter;

/**
 * Event triggered when a domain object needs approval.
 */
@Getter
public class ApprovalRequestedEvent {
    private final String referenceType;
    private final Long referenceId;
    private final Long requesterId;
    private final Long approverId;

    public ApprovalRequestedEvent(String referenceType, Long referenceId, Long requesterId, Long approverId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.requesterId = requesterId;
        this.approverId = approverId;
    }
}
