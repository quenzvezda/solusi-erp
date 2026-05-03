package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import java.util.Arrays;
import java.util.List;

public enum JournalVariable {
    GR_INVENTORY_AMT(SchemaEventType.GOODS_RECEIPT),
    GR_TAX_AMT(SchemaEventType.GOODS_RECEIPT),
    GR_GRAND_TOTAL(SchemaEventType.GOODS_RECEIPT);

    private final SchemaEventType supportedEvent;

    JournalVariable(SchemaEventType supportedEvent) {
        this.supportedEvent = supportedEvent;
    }

    public SchemaEventType getSupportedEvent() {
        return supportedEvent;
    }

    public static List<JournalVariable> getVariablesForEvent(SchemaEventType eventType) {
        return Arrays.stream(values())
                .filter(var -> var.supportedEvent == eventType)
                .toList();
    }
}