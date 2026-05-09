package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import java.util.Arrays;
import java.util.List;

public enum JournalVariable {
    GR_INVENTORY_AMT(SchemaEventType.GOODS_RECEIPT),
    GR_TAX_AMT(SchemaEventType.GOODS_RECEIPT),
    GR_GRAND_TOTAL(SchemaEventType.GOODS_RECEIPT),
    VB_GRIR_CLEARING_AMT(SchemaEventType.VENDOR_BILL),
    VB_TAX_AMT(SchemaEventType.VENDOR_BILL),
    VB_AP_TOTAL(SchemaEventType.VENDOR_BILL),
    VP_AP_AMT(SchemaEventType.VENDOR_PAYMENT),
    VP_BANK_OUT_AMT(SchemaEventType.VENDOR_PAYMENT),
    CI_AR_AMT(SchemaEventType.CUSTOMER_INVOICE),
    CI_REVENUE_AMT(SchemaEventType.CUSTOMER_INVOICE),
    CI_TAX_AMT(SchemaEventType.CUSTOMER_INVOICE),
    GI_COGS_AMT(SchemaEventType.GOODS_ISSUE),
    GI_INVENTORY_AMT(SchemaEventType.GOODS_ISSUE),
    CR_BANK_IN_AMT(SchemaEventType.CUSTOMER_RECEIPT),
    CR_AR_AMT(SchemaEventType.CUSTOMER_RECEIPT),
    SAI_INVENTORY_AMT(SchemaEventType.STOCK_ADJUSTMENT_IN),
    SAI_GAIN_AMT(SchemaEventType.STOCK_ADJUSTMENT_IN),
    SAO_LOSS_AMT(SchemaEventType.STOCK_ADJUSTMENT_OUT),
    SAO_INVENTORY_AMT(SchemaEventType.STOCK_ADJUSTMENT_OUT);

    private final SchemaEventType supportedEvent;

    JournalVariable(SchemaEventType supportedEvent) {
        this.supportedEvent = supportedEvent;
    }

    public SchemaEventType getSupportedEvent() {
        return supportedEvent;
    }

    public static List<JournalVariable> getVariablesForEvent(SchemaEventType eventType) {
        return Arrays.stream(values())
                .filter(variable -> variable.supportedEvent == eventType)
                .toList();
    }
}
