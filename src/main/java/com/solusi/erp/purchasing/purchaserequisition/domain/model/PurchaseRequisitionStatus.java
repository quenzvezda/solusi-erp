package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import java.util.Set;

public enum PurchaseRequisitionStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    CONVERTED,
    CANCELLED,
    REJECTED;

    private static final Set<PurchaseRequisitionStatus> CANCELLABLE =
        Set.of(DRAFT, SUBMITTED, APPROVED);

    public boolean canCancel() {
        return CANCELLABLE.contains(this);
    }

    public boolean canSubmit() {
        return this == DRAFT;
    }

    public boolean canUpdate() {
        return this == DRAFT;
    }

    public boolean canDelete() {
        return this == DRAFT;
    }
}
