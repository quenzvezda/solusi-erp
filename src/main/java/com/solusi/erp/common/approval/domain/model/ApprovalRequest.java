package com.solusi.erp.common.approval.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate Root: ApprovalRequest.
 * Manages the approval process for any domain object via polymorphic references.
 */
public class ApprovalRequest {
    private final AuditMetadata metadata;
    private final String referenceType;
    private final Long referenceId;
    private ApprovalStatus status;
    private Long currentApproverId;
    private List<ApprovalHistory> histories;

    public ApprovalRequest(AuditMetadata metadata, String referenceType, Long referenceId, ApprovalStatus status, Long currentApproverId) {
        this.metadata = metadata;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.status = status;
        this.currentApproverId = currentApproverId;
        this.histories = new ArrayList<>();
    }

    public static ApprovalRequest createNew(String referenceType, Long referenceId, Long requesterId, Long assignedApproverId) {
        ApprovalRequest request = new ApprovalRequest(
            AuditMetadata.empty(), referenceType, referenceId, ApprovalStatus.PENDING, assignedApproverId
        );
        request.addHistory(ApprovalAction.REQUESTED, requesterId, assignedApproverId, "Initial Request");
        return request;
    }

    public void approve(Long actorId, String notes) {
        validatePending();
        validateNotesRequired(notes);
        this.status = ApprovalStatus.COMPLETED;
        addHistory(ApprovalAction.APPROVE_AND_FINISH, actorId, null, notes);
    }

    public void approveAndForward(Long actorId, Long targetApproverId, String notes) {
        validatePending();
        validateNotesRequired(notes);
        validateForwardTarget(actorId, targetApproverId);
        this.currentApproverId = targetApproverId;
        addHistory(ApprovalAction.APPROVE_AND_FORWARD, actorId, targetApproverId, notes);
    }

    public void forward(Long actorId, Long targetApproverId, String notes) {
        validatePending();
        validateNotesRequired(notes);
        validateForwardTarget(actorId, targetApproverId);
        this.currentApproverId = targetApproverId;
        addHistory(ApprovalAction.FORWARD, actorId, targetApproverId, notes);
    }

    public void reject(Long actorId, String notes) {
        validatePending();
        validateNotesRequired(notes);
        this.status = ApprovalStatus.REJECTED;
        addHistory(ApprovalAction.REJECTED, actorId, null, notes);
    }

    private void validatePending() {
        if (this.status != ApprovalStatus.PENDING) {
            throw new DomainException("msg.error.approval.not-pending");
        }
    }

    private void validateNotesRequired(String notes) {
        if (notes == null || notes.isBlank()) {
            throw new DomainException("msg.error.approval.reason-required");
        }
    }

    private void validateForwardTarget(Long actorId, Long targetApproverId) {
        if (targetApproverId == null) {
            throw new DomainException("msg.error.approval.target-approver-required");
        }
        if (targetApproverId.equals(actorId)) {
            throw new DomainException("msg.error.approval.cannot-forward-to-self");
        }
    }

    private void addHistory(ApprovalAction action, Long actorId, Long targetApproverId, String notes) {
        this.histories.add(new ApprovalHistory(null, action, actorId, targetApproverId, notes, LocalDateTime.now()));
    }

    // Getters
    public Long getId() { return metadata.id(); }
    public String getReferenceType() { return referenceType; }
    public Long getReferenceId() { return referenceId; }
    public ApprovalStatus getStatus() { return status; }
    public Long getCurrentApproverId() { return currentApproverId; }
    public List<ApprovalHistory> getHistories() { return histories; }
    public AuditMetadata getMetadata() { return metadata; }
    
    public void setHistories(List<ApprovalHistory> histories) { this.histories = histories; }
}
