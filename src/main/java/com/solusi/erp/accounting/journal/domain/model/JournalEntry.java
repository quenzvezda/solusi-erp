package com.solusi.erp.accounting.journal.domain.model;

import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class JournalEntry {

    public static final String MANUAL_EVENT_TYPE = "MANUAL";
    public static final String MANUAL_SOURCE_TYPE = "MANUAL";

    private final AuditMetadata metadata;
    private final String eventType;
    private final String sourceType;
    private final Long sourceId;
    private final String sourceCode;
    private final Long reversalOfId;
    private LocalDate journalDate;
    private Long currencyId;
    private BigDecimal exchangeRate;
    private String referenceNo;
    private String description;
    private JournalStatus status;
    private List<JournalLine> lines;

    public JournalEntry(AuditMetadata metadata, SchemaEventType eventType, String sourceType,
                        Long sourceId, String sourceCode, LocalDate journalDate,
                        String description, JournalStatus status, List<JournalLine> lines) {
        this(metadata, eventType.name(), sourceType, sourceId, sourceCode,
                null, null, null, null, journalDate, description, status, lines);
    }

    public JournalEntry(AuditMetadata metadata, String eventType, String sourceType,
                        Long sourceId, String sourceCode, Long currencyId, BigDecimal exchangeRate,
                        String referenceNo, Long reversalOfId, LocalDate journalDate,
                        String description, JournalStatus status, List<JournalLine> lines) {
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.eventType = eventType;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.sourceCode = sourceCode;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.referenceNo = referenceNo;
        this.reversalOfId = reversalOfId;
        this.journalDate = journalDate;
        this.description = description;
        this.status = status;
        this.lines = copyLines(lines);
    }

    public static JournalEntry createPosted(SchemaEventType eventType, String sourceType, Long sourceId,
                                            String sourceCode, LocalDate journalDate, String description,
                                            List<JournalLine> lines) {
        return createPosted(eventType.name(), sourceType, sourceId, sourceCode, journalDate, description, lines);
    }

    public static JournalEntry createPosted(String eventType, String sourceType, Long sourceId,
                                            String sourceCode, LocalDate journalDate, String description,
                                            List<JournalLine> lines) {
        return new JournalEntry(
                AuditMetadata.empty(),
                eventType,
                sourceType,
                sourceId,
                sourceCode,
                null,
                null,
                null,
                null,
                journalDate,
                description,
                JournalStatus.POSTED,
                lines
        );
    }

    public static JournalEntry createDraft(LocalDate postingDate, Long currencyId, BigDecimal exchangeRate,
                                           String referenceNo, String description, List<JournalLine> lines) {
        validateManualHeader(currencyId, exchangeRate);
        List<JournalLine> copiedLines = copyManualLines(lines, currencyId, exchangeRate);
        validateBalancedInTransactionCurrency(copiedLines);
        return new JournalEntry(
                AuditMetadata.empty(),
                MANUAL_EVENT_TYPE,
                MANUAL_SOURCE_TYPE,
                null,
                null,
                currencyId,
                exchangeRate,
                referenceNo,
                null,
                postingDate,
                description,
                JournalStatus.DRAFT,
                copiedLines
        );
    }

    public void updateDraft(LocalDate postingDate, Long currencyId, BigDecimal exchangeRate,
                            String referenceNo, String description, List<JournalLine> lines) {
        requireDraft();
        validateManualHeader(currencyId, exchangeRate);
        List<JournalLine> copiedLines = copyManualLines(lines, currencyId, exchangeRate);
        validateBalancedInTransactionCurrency(copiedLines);

        this.journalDate = postingDate;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.referenceNo = referenceNo;
        this.description = description;
        this.lines = copiedLines;
    }

    public void post() {
        requireDraft();
        if (isManual()) {
            validateManualHeader(currencyId, exchangeRate);
            validateBalancedInTransactionCurrency(lines);
        } else {
            validateBalanced();
        }
        this.status = JournalStatus.POSTED;
    }

    public JournalEntry createReversal(LocalDate reversalDate, String reversalDescription) {
        if (status != JournalStatus.POSTED) {
            throw new DomainException("msg.error.journal.reversal.posted.required");
        }
        if (isReversal()) {
            throw new DomainException("msg.error.journal.reversal.chain.not.allowed");
        }
        if (getId() == null) {
            throw new DomainException("msg.error.journal.reversal.original.id.required");
        }

        List<JournalLine> reversalLines = lines.stream()
                .map(JournalLine::reversed)
                .toList();

        return new JournalEntry(
                AuditMetadata.empty(),
                eventType,
                sourceType,
                null,
                null,
                currencyId,
                exchangeRate,
                referenceNo,
                getId(),
                reversalDate,
                reversalDescription,
                JournalStatus.POSTED,
                reversalLines
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

    public boolean isManual() {
        return MANUAL_EVENT_TYPE.equals(eventType) && MANUAL_SOURCE_TYPE.equals(sourceType);
    }

    public boolean isReversal() {
        return reversalOfId != null;
    }

    public Long getId() {
        return metadata.id();
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    public String getEventType() {
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

    public Long getCurrencyId() {
        return currencyId;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public Long getReversalOfId() {
        return reversalOfId;
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

    private void requireDraft() {
        if (status != JournalStatus.DRAFT) {
            throw new DomainException("msg.error.journal.draft.required");
        }
    }

    private static List<JournalLine> copyLines(List<JournalLine> lines) {
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.journal.lines.required");
        }
        return List.copyOf(lines);
    }

    private static List<JournalLine> copyManualLines(List<JournalLine> lines, Long currencyId, BigDecimal exchangeRate) {
        List<JournalLine> copiedLines = copyLines(lines);
        if (copiedLines.size() < 2) {
            throw new DomainException("msg.error.journal.lines.minimum");
        }
        copiedLines.forEach(line -> validateManualLine(line, currencyId, exchangeRate));
        return copiedLines;
    }

    private static void validateManualHeader(Long currencyId, BigDecimal exchangeRate) {
        if (currencyId == null) {
            throw new DomainException("msg.error.journal.currency.required");
        }
        if (exchangeRate == null || exchangeRate.signum() <= 0) {
            throw new DomainException("msg.error.journal.exchange.rate.required");
        }
    }

    private static void validateManualLine(JournalLine line, Long currencyId, BigDecimal exchangeRate) {
        if (!currencyId.equals(line.originalCurrencyId())) {
            throw new DomainException("msg.error.journal.line.currency.invalid");
        }
        if (line.exchangeRate() == null || line.exchangeRate().compareTo(exchangeRate) != 0) {
            throw new DomainException("msg.error.journal.line.exchange.rate.invalid");
        }

        BigDecimal originalDebit = line.originalDebitAmount() == null ? BigDecimal.ZERO : line.originalDebitAmount();
        BigDecimal originalCredit = line.originalCreditAmount() == null ? BigDecimal.ZERO : line.originalCreditAmount();
        if ((originalDebit.signum() > 0) == (originalCredit.signum() > 0)) {
            throw new DomainException("msg.error.journal.invalid.line");
        }
    }

    private static void validateBalancedInTransactionCurrency(List<JournalLine> lines) {
        BigDecimal totalDebit = lines.stream()
                .map(line -> line.originalDebitAmount() == null ? BigDecimal.ZERO : line.originalDebitAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream()
                .map(line -> line.originalCreditAmount() == null ? BigDecimal.ZERO : line.originalCreditAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new DomainException("msg.error.journal.unbalanced");
        }
    }
}
