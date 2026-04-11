package com.solusi.erp.accounting.schema.domain.model;

/**
 * Operational events that generate automatic journal entries.
 * Each event maps to a debit/credit COA pair via AccountingSchema.
 */
public enum SchemaEventType {
    GOODS_RECEIPT,
    VENDOR_BILL,
    VENDOR_PAYMENT,
    CUSTOMER_INVOICE,
    GOODS_ISSUE,
    CUSTOMER_RECEIPT,
    STOCK_ADJUSTMENT_IN,
    STOCK_ADJUSTMENT_OUT
}
