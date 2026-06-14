package com.solusi.erp.purchasing.purchasereturn.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseReturnReason {
    DAMAGED("label.purchase-return.reason.damaged"),
    WRONG_ITEM("label.purchase-return.reason.wrong-item"),
    QUALITY_ISSUE("label.purchase-return.reason.quality-issue"),
    OVER_RECEIPT("label.purchase-return.reason.over-receipt"),
    EXPIRED("label.purchase-return.reason.expired"),
    OTHER("label.purchase-return.reason.other");

    private final String messageKey;
}
