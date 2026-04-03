package com.solusi.erp.core.event;

import lombok.Getter;

/**
 * Event triggered when a domain object needs approval.
 */
@Getter
public class ApprovalRequestedEvent {
    private final String referenceType;
    private final Long referenceId;
    private final String requester;

    public ApprovalRequestedEvent(String referenceType, Long referenceId, String requester) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.requester = requester;
    }
}
