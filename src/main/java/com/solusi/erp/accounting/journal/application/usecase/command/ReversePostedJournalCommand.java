package com.solusi.erp.accounting.journal.application.usecase.command;

import java.time.LocalDate;

public record ReversePostedJournalCommand(
        Long originalJournalEntryId,
        LocalDate reversalDate,
        String description
) {
}
