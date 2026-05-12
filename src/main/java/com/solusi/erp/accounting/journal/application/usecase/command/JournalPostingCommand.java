package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record JournalPostingCommand(
        SchemaEventType eventType,
        String sourceType,
        Long sourceId,
        String sourceCode,
        LocalDate postingDate,
        String description,
        Map<JournalVariable, BigDecimal> values,
        Long originalCurrencyId,
        BigDecimal exchangeRate,
        Map<JournalVariable, BigDecimal> originalValues
) {
    public JournalPostingCommand(SchemaEventType eventType,
                                 String sourceType,
                                 Long sourceId,
                                 String sourceCode,
                                 LocalDate postingDate,
                                 String description,
                                 Map<JournalVariable, BigDecimal> values) {
        this(eventType, sourceType, sourceId, sourceCode, postingDate, description, values, null, null, null);
    }
}
