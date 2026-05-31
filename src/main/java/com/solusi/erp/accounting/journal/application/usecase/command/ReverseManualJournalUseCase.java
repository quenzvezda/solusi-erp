package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;

import java.time.LocalDate;

public interface ReverseManualJournalUseCase {
    JournalEntry execute(Long id, LocalDate reversalDate);
}
