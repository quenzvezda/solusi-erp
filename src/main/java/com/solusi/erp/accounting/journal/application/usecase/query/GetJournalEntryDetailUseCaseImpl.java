package com.solusi.erp.accounting.journal.application.usecase.query;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.port.JournalEntryQueryPort;
import java.util.Optional;

public class GetJournalEntryDetailUseCaseImpl implements GetJournalEntryDetailUseCase {
    private final JournalEntryQueryPort queryPort;
    public GetJournalEntryDetailUseCaseImpl(JournalEntryQueryPort queryPort) {
        this.queryPort = queryPort;
    }
    @Override
    public Optional<JournalEntry> execute(Long id) {
        return queryPort.getJournalEntryDetail(id);
    }
}