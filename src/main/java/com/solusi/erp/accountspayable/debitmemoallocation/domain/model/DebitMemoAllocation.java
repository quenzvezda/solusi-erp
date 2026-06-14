package com.solusi.erp.accountspayable.debitmemoallocation.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DebitMemoAllocation {

    private final AuditMetadata metadata;
    private final String code;
    private final Long debitMemoId;
    private final String debitMemoCode;
    private LocalDate allocationDate;
    private DebitMemoAllocationStatus status;
    private Long applyJournalEntryId;
    private Long reversalJournalEntryId;
    private LocalDate reversalDate;
    private String reversalReason;
    private String notes;
    private List<DebitMemoAllocationLine> lines;

    private DebitMemoAllocation(AuditMetadata metadata,
                                String code,
                                Long debitMemoId,
                                String debitMemoCode,
                                LocalDate allocationDate,
                                DebitMemoAllocationStatus status,
                                Long applyJournalEntryId,
                                Long reversalJournalEntryId,
                                LocalDate reversalDate,
                                String reversalReason,
                                String notes,
                                List<DebitMemoAllocationLine> lines) {
        validateHeader(code, debitMemoId, debitMemoCode, allocationDate);
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.code = code;
        this.debitMemoId = debitMemoId;
        this.debitMemoCode = debitMemoCode;
        this.allocationDate = allocationDate;
        this.status = status == null ? DebitMemoAllocationStatus.DRAFT : status;
        this.applyJournalEntryId = applyJournalEntryId;
        this.reversalJournalEntryId = reversalJournalEntryId;
        this.reversalDate = reversalDate;
        this.reversalReason = reversalReason;
        this.notes = notes;
        this.lines = copyAndValidateLines(lines);
    }

    public static DebitMemoAllocation createNew(String code,
                                                Long debitMemoId,
                                                String debitMemoCode,
                                                LocalDate allocationDate,
                                                String notes,
                                                List<DebitMemoAllocationLine> lines) {
        return new DebitMemoAllocation(AuditMetadata.empty(), code, debitMemoId, debitMemoCode, allocationDate,
                DebitMemoAllocationStatus.DRAFT, null, null, null, null, notes, lines);
    }

    public static DebitMemoAllocation reconstitute(AuditMetadata metadata,
                                                   String code,
                                                   Long debitMemoId,
                                                   String debitMemoCode,
                                                   LocalDate allocationDate,
                                                   DebitMemoAllocationStatus status,
                                                   Long applyJournalEntryId,
                                                   Long reversalJournalEntryId,
                                                   LocalDate reversalDate,
                                                   String reversalReason,
                                                   String notes,
                                                   List<DebitMemoAllocationLine> lines) {
        return new DebitMemoAllocation(metadata, code, debitMemoId, debitMemoCode, allocationDate, status,
                applyJournalEntryId, reversalJournalEntryId, reversalDate, reversalReason, notes, lines);
    }

    public void update(LocalDate allocationDate, String notes, List<DebitMemoAllocationLine> lines) {
        ensureDraft("msg.error.debit-memo-allocation.edit.only-draft");
        if (allocationDate == null) {
            throw new DomainException("msg.error.debit-memo-allocation.allocation-date-required");
        }
        this.allocationDate = allocationDate;
        this.notes = notes;
        this.lines = copyAndValidateLines(lines);
    }

    public void confirm(Long applyJournalEntryId) {
        ensureDraft("msg.error.debit-memo-allocation.confirm.only-draft");
        this.applyJournalEntryId = applyJournalEntryId;
        this.status = DebitMemoAllocationStatus.CONFIRMED;
    }

    public void cancel() {
        ensureDraft("msg.error.debit-memo-allocation.cancel.only-draft");
        this.status = DebitMemoAllocationStatus.CANCELLED;
    }

    public void reverse(Long reversalJournalEntryId, LocalDate reversalDate, String reversalReason) {
        if (this.status != DebitMemoAllocationStatus.CONFIRMED) {
            throw new DomainException("msg.error.debit-memo-allocation.reverse.only-confirmed");
        }
        if (reversalDate == null) {
            throw new DomainException("msg.error.debit-memo-allocation.reversal-date-required");
        }
        this.reversalJournalEntryId = reversalJournalEntryId;
        this.reversalDate = reversalDate;
        this.reversalReason = reversalReason;
        this.status = DebitMemoAllocationStatus.REVERSED;
    }

    private void ensureDraft(String messageKey) {
        if (this.status != DebitMemoAllocationStatus.DRAFT) {
            throw new DomainException(messageKey);
        }
    }

    private static void validateHeader(String code, Long debitMemoId, String debitMemoCode, LocalDate allocationDate) {
        if (isBlank(code)) {
            throw new DomainException("msg.error.debit-memo-allocation.code-required");
        }
        if (debitMemoId == null || isBlank(debitMemoCode)) {
            throw new DomainException("msg.error.debit-memo-allocation.debit-memo-required");
        }
        if (allocationDate == null) {
            throw new DomainException("msg.error.debit-memo-allocation.allocation-date-required");
        }
    }

    private static List<DebitMemoAllocationLine> copyAndValidateLines(List<DebitMemoAllocationLine> sourceLines) {
        if (sourceLines == null || sourceLines.isEmpty()) {
            throw new DomainException("msg.error.debit-memo-allocation.lines-required");
        }

        List<DebitMemoAllocationLine> copiedLines = List.copyOf(sourceLines);
        Set<Long> vendorBillIds = new HashSet<>();
        for (DebitMemoAllocationLine line : copiedLines) {
            if (!vendorBillIds.add(line.getVendorBillId())) {
                throw new DomainException("msg.error.debit-memo-allocation.duplicate-vendor-bill");
            }
        }

        BigDecimal totalApplied = copiedLines.stream()
                .map(DebitMemoAllocationLine::getAppliedGrossOriginal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalApplied.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.debit-memo-allocation.line.amount-positive");
        }
        BigDecimal debitMemoRemaining = copiedLines.get(0).getDebitMemoRemainingAtDraft();
        if (totalApplied.compareTo(debitMemoRemaining) > 0) {
            throw new DomainException("msg.error.debit-memo-allocation.over-debit-memo-remaining");
        }

        return copiedLines;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private BigDecimal sum(java.util.function.Function<DebitMemoAllocationLine, BigDecimal> mapper) {
        return lines.stream().map(mapper).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public Long getDebitMemoId() { return debitMemoId; }
    public String getDebitMemoCode() { return debitMemoCode; }
    public LocalDate getAllocationDate() { return allocationDate; }
    public DebitMemoAllocationStatus getStatus() { return status; }
    public Long getApplyJournalEntryId() { return applyJournalEntryId; }
    public Long getReversalJournalEntryId() { return reversalJournalEntryId; }
    public LocalDate getReversalDate() { return reversalDate; }
    public String getReversalReason() { return reversalReason; }
    public String getNotes() { return notes; }
    public List<DebitMemoAllocationLine> getLines() { return lines; }
    public BigDecimal getTotalAppliedGrossOriginal() { return sum(DebitMemoAllocationLine::getAppliedGrossOriginal); }
    public BigDecimal getTotalDppOriginal() { return sum(DebitMemoAllocationLine::getAppliedDppOriginal); }
    public BigDecimal getTotalTaxOriginal() { return sum(DebitMemoAllocationLine::getAppliedTaxOriginal); }
    public BigDecimal getTotalGrirReversalBase() { return sum(DebitMemoAllocationLine::getGrirReversalBase); }
    public BigDecimal getTotalTaxReversalBase() { return sum(DebitMemoAllocationLine::getTaxReversalBase); }
    public BigDecimal getTotalApReductionBase() { return sum(DebitMemoAllocationLine::getApReductionBase); }
    public BigDecimal getTotalFxLossBase() { return sum(DebitMemoAllocationLine::getFxLossBase); }
    public BigDecimal getTotalFxGainBase() { return sum(DebitMemoAllocationLine::getFxGainBase); }
}
