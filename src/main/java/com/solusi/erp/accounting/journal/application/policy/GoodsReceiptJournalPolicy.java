package com.solusi.erp.accounting.journal.application.policy;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoodsReceiptJournalPolicy implements JournalPolicy {

    @Override
    public SchemaEventType supports() {
        return SchemaEventType.GOODS_RECEIPT;
    }

    @Override
    public List<JournalLine> buildLines(AccountingSchema schema, JournalPostingCommand cmd) {
        // 1. Prepare variable map from command
        Map<JournalVariable, BigDecimal> variableValues = Map.of(
                JournalVariable.GR_INVENTORY_AMT, cmd.inventoryAmount(),
                JournalVariable.GR_TAX_AMT, cmd.taxAmount(),
                JournalVariable.GR_GRAND_TOTAL, cmd.totalAmount()
        );

        List<JournalLine> finalLines = new ArrayList<>();

        // 2. Evaluate schema lines
        for (AccountingSchemaLine schemaLine : schema.getLines()) {
            BigDecimal value = variableValues.get(schemaLine.variable());
            
            // ZERO SKIPPING LOGIC
            if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
                continue; 
            }

            if (schemaLine.position() == JournalPosition.DEBIT) {
                finalLines.add(JournalLine.debit(schemaLine.accountId(), value));
            } else {
                finalLines.add(JournalLine.credit(schemaLine.accountId(), value));
            }
        }

        return finalLines;
    }
}