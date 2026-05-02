package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.application.policy.JournalPolicy;
import com.solusi.erp.accounting.journal.application.policy.JournalPolicyResolver;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.exception.DomainException;

import java.util.List;

public class PostJournalForEventUseCaseImpl implements PostJournalForEventUseCase {

    private final SchemaRepository schemaRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final JournalPolicyResolver policyResolver;

    public PostJournalForEventUseCaseImpl(SchemaRepository schemaRepository,
                                          JournalEntryRepository journalEntryRepository,
                                          JournalPolicyResolver policyResolver) {
        this.schemaRepository = schemaRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.policyResolver = policyResolver;
    }

    @Override
    public void execute(JournalPostingCommand command) {
        if (journalEntryRepository.existsBySource(command.sourceType(), command.sourceId())) {
            return;
        }

        AccountingSchema schema = schemaRepository.findByEventTypeAndIsActiveTrue(command.eventType())
                .orElseThrow(() -> new DomainException("msg.error.journal.schema.notfound"));

        JournalPolicy policy = policyResolver.resolve(command.eventType());
        List<JournalLine> lines = policy.buildLines(schema, command);

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
