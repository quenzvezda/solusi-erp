package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.exception.DomainException;

import com.solusi.erp.accounting.journal.domain.model.JournalVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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
                    BigDecimal value = command.values().getOrDefault(schemaLine.getVar(), BigDecimal.ZERO);
                    if (value == null || value.compareTo(BigDecimal.ZERO) == 0) {
                        return null;
                    }
                    Long accountId = resolveAccountId(command, schemaLine.getVar(), schemaLine.getAccountId());
                    if (command.originalCurrencyId() != null && command.originalValues() != null) {
                        BigDecimal originalValue = command.originalValues().getOrDefault(schemaLine.getVar(), value);
                        return schemaLine.getPosition() == JournalPosition.DEBIT
                                ? JournalLine.debitWithOriginal(accountId, value,
                                command.originalCurrencyId(), command.exchangeRate(), originalValue)
                                : JournalLine.creditWithOriginal(accountId, value,
                                command.originalCurrencyId(), command.exchangeRate(), originalValue);
                    }
                    return schemaLine.getPosition() == JournalPosition.DEBIT
                            ? JournalLine.debit(accountId, value)
                            : JournalLine.credit(accountId, value);
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

    private Long resolveAccountId(JournalPostingCommand command, JournalVariable variable, Long schemaAccountId) {
        Map<JournalVariable, Long> overrides = command.accountOverrides();
        if (overrides != null && overrides.containsKey(variable)) {
            return overrides.get(variable);
        }
        return schemaAccountId;
    }
}
