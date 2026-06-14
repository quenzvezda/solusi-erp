package com.solusi.erp.purchasing.purchasereturn.domain.model;

public enum PurchaseReturnStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REJECTED,
    CANCELLED,
    CONFIRMED,
    REVERSED;

    public boolean canEdit() {
        return this == DRAFT;
    }

    public boolean canSubmit() {
        return this == DRAFT;
    }

    public boolean canApprove() {
        return this == SUBMITTED;
    }

    public boolean canReject() {
        return this == SUBMITTED;
    }

    public boolean canCancelDraft() {
        return this == DRAFT;
    }

    public boolean canCancelSubmission() {
        return this == SUBMITTED;
    }

    public boolean canCancelApproved() {
        return this == APPROVED;
    }

    public boolean canConfirm() {
        return this == APPROVED;
    }

    public boolean canReverse() {
        return this == CONFIRMED;
    }
}
