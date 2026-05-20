package com.solusi.erp.core.event;

import lombok.Getter;

/**
 * Event triggered when an approval request is rejected.
 */
@Getter
public class ApprovalRejectedEvent {
    private final String referenceType;
    private final Long referenceId;

    public ApprovalRejectedEvent(String referenceType, Long referenceId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }
}
