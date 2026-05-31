package com.solusi.erp.accounting.journal.domain.model;

import java.time.LocalDate;

public record JournalEntryFilter(
        String sourceType,
        String sourceCode,
        String journalCode,
        LocalDate postingDateFrom,
        LocalDate postingDateTo
) {
}
