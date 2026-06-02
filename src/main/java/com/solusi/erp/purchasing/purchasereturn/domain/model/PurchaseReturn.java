package com.solusi.erp.purchasing.purchasereturn.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class PurchaseReturn {

    public static final String GOODS_RECEIPT_REFERENCE_TYPE = "GOODS_RECEIPT";

    private final AuditMetadata metadata;
    private final String code;
    private LocalDate returnDate;
    private final String referenceType;
    private final Long referenceId;
    private final String referenceCode;
    private final Long purchaseOrderId;
    private final String purchaseOrderCode;
    private final Long supplierId;
    private final Long facilityId;
    private final Long currencyId;
    private final BigDecimal exchangeRate;
    private PurchaseReturnStatus status;
    private PurchaseReturnReason reason;
    private String note;
    private Long submittedByUserId;
    private Long generatedGoodsIssueId;
    private List<PurchaseReturnLine> lines;

    private PurchaseReturn(AuditMetadata metadata,
                           String code,
                           LocalDate returnDate,
                           String referenceType,
                           Long referenceId,
                           String referenceCode,
                           Long purchaseOrderId,
                           String purchaseOrderCode,
                           Long supplierId,
                           Long facilityId,
                           Long currencyId,
                           BigDecimal exchangeRate,
                           PurchaseReturnStatus status,
                           PurchaseReturnReason reason,
                           String note,
                           Long submittedByUserId,
                           Long generatedGoodsIssueId,
                           List<PurchaseReturnLine> lines) {
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.code = code;
        this.returnDate = returnDate;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.referenceCode = referenceCode;
        this.purchaseOrderId = purchaseOrderId;
        this.purchaseOrderCode = purchaseOrderCode;
        this.supplierId = supplierId;
        this.facilityId = facilityId;
        this.currencyId = currencyId;
        this.exchangeRate = exchangeRate;
        this.status = status;
        this.reason = reason;
        this.note = note;
        this.submittedByUserId = submittedByUserId;
        this.generatedGoodsIssueId = generatedGoodsIssueId;
        this.lines = copyLines(lines);
    }

    public static PurchaseReturn createNew(String code,
                                           LocalDate returnDate,
                                           String referenceType,
                                           Long referenceId,
                                           String referenceCode,
                                           Long purchaseOrderId,
                                           String purchaseOrderCode,
                                           Long supplierId,
                                           Long facilityId,
                                           Long currencyId,
                                           BigDecimal exchangeRate,
                                           PurchaseReturnReason reason,
                                           String note,
                                           List<PurchaseReturnLine> lines) {
        validateHeader(referenceType, referenceId, reason, note, lines);
        return new PurchaseReturn(
                AuditMetadata.empty(), code, returnDate, referenceType, referenceId, referenceCode,
                purchaseOrderId, purchaseOrderCode, supplierId, facilityId, currencyId, exchangeRate,
                PurchaseReturnStatus.DRAFT, reason, note, null, null, lines
        );
    }

    public static PurchaseReturn reconstitute(AuditMetadata metadata,
                                              String code,
                                              LocalDate returnDate,
                                              String referenceType,
                                              Long referenceId,
                                              String referenceCode,
                                              Long purchaseOrderId,
                                              String purchaseOrderCode,
                                              Long supplierId,
                                              Long facilityId,
                                              Long currencyId,
                                              BigDecimal exchangeRate,
                                              PurchaseReturnStatus status,
                                              PurchaseReturnReason reason,
                                              String note,
                                              Long submittedByUserId,
                                              Long generatedGoodsIssueId,
                                              List<PurchaseReturnLine> lines) {
        return new PurchaseReturn(
                metadata, code, returnDate, referenceType, referenceId, referenceCode, purchaseOrderId,
                purchaseOrderCode, supplierId, facilityId, currencyId, exchangeRate, status, reason,
                note, submittedByUserId, generatedGoodsIssueId, lines
        );
    }

    public void updateDraft(LocalDate returnDate,
                            PurchaseReturnReason reason,
                            String note,
                            List<PurchaseReturnLine> lines) {
        if (!status.canEdit()) {
            throw new DomainException("msg.error.purchase-return.update.not-draft");
        }
        validateHeader(referenceType, referenceId, reason, note, lines);
        this.returnDate = returnDate;
        this.reason = reason;
        this.note = note;
        this.lines = copyLines(lines);
    }

    public void submit(Long submitterUserId) {
        if (!status.canSubmit()) {
            throw new DomainException("msg.error.purchase-return.submit.invalid-status");
        }
        if (submitterUserId == null) {
            throw new DomainException("msg.error.purchase-return.submit.user-required");
        }
        if (lines.isEmpty()) {
            throw new DomainException("msg.error.purchase-return.lines-required");
        }
        submittedByUserId = submitterUserId;
        status = PurchaseReturnStatus.SUBMITTED;
    }

    public void approve() {
        if (!status.canApprove()) {
            throw new DomainException("msg.error.purchase-return.approve.invalid-status");
        }
        status = PurchaseReturnStatus.APPROVED;
    }

    public void reject() {
        if (!status.canReject()) {
            throw new DomainException("msg.error.purchase-return.reject.invalid-status");
        }
        status = PurchaseReturnStatus.REJECTED;
    }

    public void cancelDraft() {
        if (!status.canCancelDraft()) {
            throw new DomainException("msg.error.purchase-return.cancel-draft.invalid-status");
        }
        status = PurchaseReturnStatus.CANCELLED;
    }

    public void cancelSubmission(Long actorUserId) {
        if (!status.canCancelSubmission()) {
            throw new DomainException("msg.error.purchase-return.cancel-submission.invalid-status");
        }
        if (actorUserId == null || !actorUserId.equals(submittedByUserId)) {
            throw new DomainException("msg.error.purchase-return.cancel-submission.creator-only");
        }
        status = PurchaseReturnStatus.CANCELLED;
    }

    public void cancelApproved() {
        if (!status.canCancelApproved()) {
            throw new DomainException("msg.error.purchase-return.cancel-approved.invalid-status");
        }
        status = PurchaseReturnStatus.CANCELLED;
    }

    public void confirm(Long generatedGoodsIssueId) {
        if (!status.canConfirm()) {
            throw new DomainException("msg.error.purchase-return.confirm.invalid-status");
        }
        if (generatedGoodsIssueId == null) {
            throw new DomainException("msg.error.purchase-return.confirm.gi-required");
        }
        this.generatedGoodsIssueId = generatedGoodsIssueId;
        status = PurchaseReturnStatus.CONFIRMED;
    }

    private static void validateHeader(String referenceType,
                                       Long referenceId,
                                       PurchaseReturnReason reason,
                                       String note,
                                       List<PurchaseReturnLine> lines) {
        if (!GOODS_RECEIPT_REFERENCE_TYPE.equals(referenceType) || referenceId == null) {
            throw new DomainException("msg.error.purchase-return.source.goods-receipt-required");
        }
        if (reason == null) {
            throw new DomainException("msg.error.purchase-return.reason-required");
        }
        if (reason == PurchaseReturnReason.OTHER && isBlank(note)) {
            throw new DomainException("msg.error.purchase-return.other-note-required");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.purchase-return.lines-required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static List<PurchaseReturnLine> copyLines(List<PurchaseReturnLine> lines) {
        return lines == null ? List.of() : List.copyOf(lines);
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public LocalDate getReturnDate() { return returnDate; }
    public String getReferenceType() { return referenceType; }
    public Long getReferenceId() { return referenceId; }
    public String getReferenceCode() { return referenceCode; }
    public Long getPurchaseOrderId() { return purchaseOrderId; }
    public String getPurchaseOrderCode() { return purchaseOrderCode; }
    public Long getSupplierId() { return supplierId; }
    public Long getFacilityId() { return facilityId; }
    public Long getCurrencyId() { return currencyId; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public PurchaseReturnStatus getStatus() { return status; }
    public PurchaseReturnReason getReason() { return reason; }
    public String getNote() { return note; }
    public Long getSubmittedByUserId() { return submittedByUserId; }
    public Long getGeneratedGoodsIssueId() { return generatedGoodsIssueId; }
    public List<PurchaseReturnLine> getLines() { return Collections.unmodifiableList(lines); }
}
