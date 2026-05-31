package com.solusi.erp.accounting.journal.application.usecase.query;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;

public record JournalEntryDetailView(
        JournalEntry entry,
        Long reversedById
) {
}
