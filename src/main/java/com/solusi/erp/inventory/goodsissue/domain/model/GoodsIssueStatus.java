package com.solusi.erp.inventory.goodsissue.domain.model;

public enum GoodsIssueStatus {
    DRAFT,
    COMPLETED,
    CANCELLED;

    public boolean isEditable() {
        return this == DRAFT;
    }

    public boolean canComplete() {
        return this == DRAFT;
    }

    public boolean canCancel() {
        return this == COMPLETED;
    }
}
