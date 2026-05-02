package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class JournalEntry {

    private final AuditMetadata metadata;
    private final SchemaEventType eventType;
    private final String sourceType;
    private final Long sourceId;
    private final String sourceCode;
    private final LocalDate journalDate;
    private final String description;
    private final JournalStatus status;
    private final List<JournalLine> lines;

    public JournalEntry(AuditMetadata metadata, SchemaEventType eventType, String sourceType,
                        Long sourceId, String sourceCode, LocalDate journalDate,
                        String description, JournalStatus status, List<JournalLine> lines) {
        this.metadata = metadata;
        this.eventType = eventType;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.sourceCode = sourceCode;
        this.journalDate = journalDate;
        this.description = description;
        this.status = status;
        this.lines = copyLines(lines);
    }

    public static JournalEntry createPosted(SchemaEventType eventType, String sourceType, Long sourceId,
                                            String sourceCode, LocalDate journalDate, String description,
                                            List<JournalLine> lines) {
        return new JournalEntry(
                AuditMetadata.empty(),
                eventType,
                sourceType,
                sourceId,
                sourceCode,
                journalDate,
                description,
                JournalStatus.POSTED,
                lines
        );
    }

    public void validateBalanced() {
        BigDecimal totalDebit = lines.stream()
                .map(line -> line.debitAmount() == null ? BigDecimal.ZERO : line.debitAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream()
                .map(line -> line.creditAmount() == null ? BigDecimal.ZERO : line.creditAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new DomainException("msg.error.journal.unbalanced");
        }
    }

    public Long getId() {
        return metadata.id();
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    public SchemaEventType getEventType() {
        return eventType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public LocalDate getJournalDate() {
        return journalDate;
    }

    public String getDescription() {
        return description;
    }

    public JournalStatus getStatus() {
        return status;
    }

    public List<JournalLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private static List<JournalLine> copyLines(List<JournalLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.journal.lines.required");
        }
        return List.copyOf(lines);
    }
}
