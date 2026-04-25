package com.solusi.erp.purchasing.purchaseorder.domain.model;

import java.util.Set;

public enum PurchaseOrderStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    SENT,
    PARTIALLY_RECEIVED,
    FULLY_RECEIVED,
    BILLED,
    CLOSED,
    CANCELLED,
    REJECTED;

    private static final Set<PurchaseOrderStatus> CANCELLABLE =
        Set.of(DRAFT, SUBMITTED);

    private static final Set<PurchaseOrderStatus> RECEIVABLE =
        Set.of(SENT, PARTIALLY_RECEIVED);

    public boolean canUpdate() {
        return this == DRAFT;
    }

    public boolean canSubmit() {
        return this == DRAFT;
    }

    public boolean canSend() {
        return this == APPROVED;
    }

    public boolean canCancel() {
        return CANCELLABLE.contains(this);
    }

    public boolean canDelete() {
        return this == DRAFT;
    }

    public boolean canReceive() {
        return RECEIVABLE.contains(this);
    }

    public boolean isEditable() {
        return this == DRAFT;
    }
}
