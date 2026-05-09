package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import java.time.LocalDate;

public record JournalEntryFilter(
        SchemaEventType sourceType,
        String sourceCode,
        String journalCode,
        LocalDate postingDateFrom,
        LocalDate postingDateTo
) {
}