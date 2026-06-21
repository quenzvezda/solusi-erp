package com.solusi.erp.core.event;

import lombok.Getter;

/**
 * Event triggered when an approval request is completed (approved).
 */
@Getter
public class ApprovalCompletedEvent {
    private final String referenceType;
    private final Long referenceId;
    private final Long actorId;

    public ApprovalCompletedEvent(String referenceType, Long referenceId) {
        this(referenceType, referenceId, null);
    }

    public ApprovalCompletedEvent(String referenceType, Long referenceId, Long actorId) {
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.actorId = actorId;
    }
}
