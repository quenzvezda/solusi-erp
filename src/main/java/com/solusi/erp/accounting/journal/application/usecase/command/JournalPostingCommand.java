package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;

public record JournalPostingCommand(
        SchemaEventType eventType,
        String sourceType,
        Long sourceId,
        String sourceCode,
        LocalDate postingDate,
        String description,
        BigDecimal inventoryAmount,
        BigDecimal taxAmount,
        BigDecimal totalAmount
) {
    public JournalPostingCommand {
        if (inventoryAmount.add(taxAmount).compareTo(totalAmount) != 0) {
            throw new DomainException("msg.error.journal.unbalanced");
        }
    }
}
