package com.solusi.erp.inventory.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Reference Type Enum for Source Documents.
 */
@Getter
@RequiredArgsConstructor
public enum ReferenceType {
    GOODS_RECEIPT("enum.reference.type.goods_receipt"),
    GOODS_ISSUE("enum.reference.type.goods_issue"),
    SALES_ORDER("enum.reference.type.sales_order"),
    DELIVERY_ORDER("enum.reference.type.delivery_order"),
    STOCK_ADJUSTMENT("enum.reference.type.stock_adjustment"),
    STOCK_OPNAME("enum.reference.type.stock_opname"),
    INTERNAL_TRANSFER("enum.reference.type.internal_transfer"),
    MANUAL("enum.reference.type.manual");

    private final String messageKey;
}
