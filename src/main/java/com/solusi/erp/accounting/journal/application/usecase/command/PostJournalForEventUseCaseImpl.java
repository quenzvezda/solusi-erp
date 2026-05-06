package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class PostJournalForEventUseCaseImpl implements PostJournalForEventUseCase {

    private final SchemaRepository schemaRepository;
    private final JournalEntryRepository journalEntryRepository;

    public PostJournalForEventUseCaseImpl(SchemaRepository schemaRepository,
                                          JournalEntryRepository journalEntryRepository) {
        this.schemaRepository = schemaRepository;
        this.journalEntryRepository = journalEntryRepository;
    }

    @Override
    public void execute(JournalPostingCommand command) {
        if (journalEntryRepository.existsBySource(command.sourceType(), command.sourceId())) {
            return;
        }

        AccountingSchema schema = schemaRepository.findByEventTypeAndIsActiveTrue(command.eventType())
                .orElseThrow(() -> new DomainException("msg.error.journal.schema.notfound"));

        List<JournalLine> lines = schema.getLines().stream()
                .map(schemaLine -> {
                    BigDecimal value = command.values().getOrDefault(schemaLine.variable(), BigDecimal.ZERO);
                    if (value.compareTo(BigDecimal.ZERO) == 0) return null;
                    return schemaLine.position() == JournalPosition.DEBIT
                            ? JournalLine.debit(schemaLine.accountId(), value)
                            : JournalLine.credit(schemaLine.accountId(), value);
                })
                .filter(Objects::nonNull)
                .toList();

        JournalEntry entry = JournalEntry.createPosted(
                command.eventType(),
                command.sourceType(),
                command.sourceId(),
                command.sourceCode(),
                command.postingDate(),
                command.description(),
                lines
        );
        entry.validateBalanced();
        journalEntryRepository.save(entry);
    }
}
