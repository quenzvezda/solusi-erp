package com.solusi.erp.inventory.goodsissue.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class GoodsIssue {

    private final AuditMetadata metadata;
    private final String code;
    private LocalDate issueDate;
    private final GoodsIssueReferenceType referenceType;
    private final Long referenceId;
    private final String referenceCode;
    private final Long partyId;
    private final GoodsIssuePartyType partyType;
    private final Long facilityId;
    private final Long currencyId;
    private final BigDecimal exchangeRate;
    private GoodsIssueStatus status;
    private String note;
    private List<GoodsIssueLine> lines;

    public GoodsIssue(AuditMetadata metadata, String code, LocalDate issueDate,
                      GoodsIssueReferenceType referenceType, Long referenceId, String referenceCode,
                      Long partyId, GoodsIssuePartyType partyType, Long facilityId, Long currencyId,
                      BigDecimal exchangeRate, GoodsIssueStatus status, String note,
                      List<GoodsIssueLine> lines) {
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.code = code;
        this.issueDate = issueDate;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.referenceCode = referenceCode;
        this.partyId = partyId;
        this.partyType = partyType;
        this.facilityId = facilityId;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.status = status;
        this.note = note;
        this.lines = copyLines(lines);
    }

    public static GoodsIssue createNew(String code, LocalDate issueDate,
                                       GoodsIssueReferenceType referenceType, Long referenceId,
                                       String referenceCode, Long partyId, GoodsIssuePartyType partyType,
                                       Long facilityId, Long currencyId, BigDecimal exchangeRate,
                                       List<GoodsIssueLine> lines) {
        return new GoodsIssue(
                AuditMetadata.empty(), code, issueDate, referenceType, referenceId, referenceCode,
                partyId, partyType, facilityId, currencyId, exchangeRate, GoodsIssueStatus.DRAFT,
                null, lines
        );
    }

    public void update(LocalDate issueDate, String note, List<GoodsIssueLine> lines) {
        ensureEditable();
        this.issueDate = issueDate;
        this.note = note;
        this.lines = copyLines(lines);
    }

    public void complete() {
        if (status == GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.gi.completed.immutable");
        }
        if (status == GoodsIssueStatus.CANCELLED) {
            throw new DomainException("msg.error.gi.cancelled.immutable");
        }
        boolean hasPositiveLine = lines.stream().anyMatch(GoodsIssueLine::hasIssueQuantity);
        if (!hasPositiveLine) {
            throw new DomainException("msg.error.gi.complete.no.lines");
        }
        this.status = GoodsIssueStatus.COMPLETED;
    }

    public void cancel() {
        if (status == GoodsIssueStatus.CANCELLED) {
            throw new DomainException("msg.error.gi.cancelled.immutable");
        }
        if (!status.canCancel()) {
            throw new DomainException("msg.error.gi.cancel.only.completed");
        }
        this.status = GoodsIssueStatus.CANCELLED;
    }

    public Long getId() {
        return metadata.id();
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    public String getCode() {
        return code;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public GoodsIssueReferenceType getReferenceType() {
        return referenceType;
    }

    public Long getReferenceId() {
        return referenceId;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public Long getPartyId() {
        return partyId;
    }

    public GoodsIssuePartyType getPartyType() {
        return partyType;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public GoodsIssueStatus getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public List<GoodsIssueLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private void ensureEditable() {
        if (status == GoodsIssueStatus.COMPLETED) {
            throw new DomainException("msg.error.gi.completed.immutable");
        }
        if (status == GoodsIssueStatus.CANCELLED) {
            throw new DomainException("msg.error.gi.cancelled.immutable");
        }
    }

    private static List<GoodsIssueLine> copyLines(List<GoodsIssueLine> lines) {
        return lines == null ? List.of() : List.copyOf(lines);
    }
}
