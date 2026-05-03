package com.solusi.erp.accounting.journal.application.usecase.query;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import java.util.Optional;

public interface GetJournalEntryDetailUseCase {
    Optional<JournalEntry> execute(Long id);
}