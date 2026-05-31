package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;

public interface UpdateManualJournalUseCase {
    JournalEntry execute(Long id, ManualJournalCommand command);
}
