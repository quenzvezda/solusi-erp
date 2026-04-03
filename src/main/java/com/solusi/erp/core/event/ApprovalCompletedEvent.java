package com.solusi.erp.core.event;

import lombok.Getter;

/**
 * Event triggered when an approval request is completed (approved).
 */
@Getter
public class ApprovalCompletedEvent {
    private final String referenceType;
    private final Long referenceId;

    public ApprovalCompletedEvent(String referenceType, Long referenceId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }
}
