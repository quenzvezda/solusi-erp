package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PurchaseRequisition {

    private final AuditMetadata metadata;
    private final String code;
    private LocalDate requestDate;
    private final Long requesterId;
    private Long facilityId;
    private String department;
    private PurchaseRequisitionPriority priority;
    private PurchaseRequisitionStatus status;
    private String note;
    private boolean active;
    private Long suggestedSupplierId;
    private List<PurchaseRequisitionLine> lines;

    public PurchaseRequisition(AuditMetadata metadata, String code,
                                LocalDate requestDate, Long requesterId,
                                Long facilityId, String department,
                                PurchaseRequisitionPriority priority,
                                PurchaseRequisitionStatus status,
                                String note, boolean active,
                                Long suggestedSupplierId,
                                List<PurchaseRequisitionLine> lines) {
        this.metadata = metadata;
        this.code = code;
        this.requestDate = requestDate;
        this.requesterId = requesterId;
        this.facilityId = facilityId;
        this.department = department;
        this.priority = priority;
        this.status = status;
        this.note = note;
        this.active = active;
        this.suggestedSupplierId = suggestedSupplierId;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public static PurchaseRequisition createNew(String code, LocalDate requestDate,
                                                 Long requesterId, Long facilityId,
                                                 String department,
                                                 PurchaseRequisitionPriority priority,
                                                 String note,
                                                 Long suggestedSupplierId,
                                                 List<PurchaseRequisitionLine> lines) {
        return new PurchaseRequisition(
            AuditMetadata.empty(), code, requestDate, requesterId,
            facilityId, department, priority,
            PurchaseRequisitionStatus.DRAFT,
            note, true, suggestedSupplierId, lines
        );
    }

    public void update(LocalDate requestDate, Long facilityId,
                       String department, PurchaseRequisitionPriority priority,
                       String note, Long suggestedSupplierId,
                       List<PurchaseRequisitionLine> lines) {
        if (!status.canUpdate()) {
            throw new DomainException("msg.error.pr.update.not.draft");
        }
        this.requestDate = requestDate;
        this.facilityId = facilityId;
        this.department = department;
        this.priority = priority;
        this.note = note;
        this.suggestedSupplierId = suggestedSupplierId;
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
    }

    public void submit() {
        if (!status.canSubmit()) {
            throw new DomainException("msg.error.pr.submit.invalid.status");
        }
        if (lines == null || lines.isEmpty()) {
            throw new DomainException("msg.error.pr.submit.no.lines");
        }
        this.status = PurchaseRequisitionStatus.SUBMITTED;
    }

    public void approve() {
        this.status = PurchaseRequisitionStatus.APPROVED;
    }

    public void reject() {
        this.status = PurchaseRequisitionStatus.REJECTED;
    }

    public void cancel() {
        if (!status.canCancel()) {
            throw new DomainException("msg.error.pr.cancel.invalid.status");
        }
        this.status = PurchaseRequisitionStatus.CANCELLED;
    }

    public void deactivate() {
        if (!status.canDelete()) {
            throw new DomainException("msg.error.pr.delete.not.draft");
        }
        this.active = false;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public LocalDate getRequestDate() { return requestDate; }
    public Long getRequesterId() { return requesterId; }
    public Long getFacilityId() { return facilityId; }
    public String getDepartment() { return department; }
    public PurchaseRequisitionPriority getPriority() { return priority; }
    public PurchaseRequisitionStatus getStatus() { return status; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
    public Long getSuggestedSupplierId() { return suggestedSupplierId; }
    public List<PurchaseRequisitionLine> getLines() { return Collections.unmodifiableList(lines); }
}
