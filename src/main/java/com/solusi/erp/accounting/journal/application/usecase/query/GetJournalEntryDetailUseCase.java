package com.solusi.erp.accounting.journal.application.usecase.query;

import java.util.Optional;

public interface GetJournalEntryDetailUseCase {
    Optional<JournalEntryDetailView> execute(Long id);
}
