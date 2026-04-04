package com.solusi.erp.common.approval.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApprovalRequestTest {

    @Test
    @DisplayName("Should create a new ApprovalRequest with PENDING status and initial history")
    void shouldCreateNewApprovalRequest() {
        String refType = "NEWS";
        Long refId = 123L;
        Long requesterId = 1L;

        Long approverId = 50L;

        ApprovalRequest request = ApprovalRequest.createNew(refType, refId, requesterId, approverId);

        assertNotNull(request);
        assertEquals(refType, request.getReferenceType());
        assertEquals(refId, request.getReferenceId());
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
        assertEquals(approverId, request.getCurrentApproverId());
        assertEquals(1, request.getHistories().size());
        
        ApprovalHistory history = request.getHistories().get(0);
        assertEquals(ApprovalAction.REQUESTED, history.action());
        assertEquals(requesterId, history.actorId());
        assertEquals("Initial Request", history.notes());
    }

    @Test
    @DisplayName("Should change status to COMPLETED when approved")
    void shouldApprove() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L, 50L);
        Long approverId = 2L;
        String notes = "Approved!";

        request.approve(approverId, notes);

        assertEquals(ApprovalStatus.COMPLETED, request.getStatus());
        assertEquals(2, request.getHistories().size());
        
        ApprovalHistory latestHistory = request.getHistories().get(1);
        assertEquals(ApprovalAction.APPROVE_AND_FINISH, latestHistory.action());
        assertEquals(approverId, latestHistory.actorId());
        assertEquals(notes, latestHistory.notes());
    }

    @Test
    @DisplayName("Should change status to REJECTED when rejected")
    void shouldReject() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L, 50L);
        Long actorId = 2L;
        String notes = "Rejected due to typo";

        request.reject(actorId, notes);

        assertEquals(ApprovalStatus.REJECTED, request.getStatus());
        assertEquals(2, request.getHistories().size());
        
        ApprovalHistory latestHistory = request.getHistories().get(1);
        assertEquals(ApprovalAction.REJECTED, latestHistory.action());
        assertEquals(actorId, latestHistory.actorId());
        assertEquals(notes, latestHistory.notes());
    }

    @Test
    @DisplayName("Should throw exception when approving a non-PENDING request")
    void shouldThrowExceptionWhenApprovingNonPending() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L, 50L);
        request.approve(2L, "First Approval");

        DomainException exception = assertThrows(DomainException.class, () -> 
            request.approve(2L, "Second Approval")
        );
        
        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should throw exception when rejecting a non-PENDING request")
    void shouldThrowExceptionWhenRejectingNonPending() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L, 50L);
        request.reject(2L, "First Rejection");

        DomainException exception = assertThrows(DomainException.class, () -> 
            request.reject(2L, "Second Rejection")
        );
        
        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should throw exception when approving an already REJECTED request (not-pending)")
    void shouldThrowExceptionWhenApprovingRejectedRequest() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L, 50L);
        request.reject(2L, "Rejected notes");

        DomainException exception = assertThrows(DomainException.class, () ->
            request.approve(2L, "Trying to approve after reject")
        );

        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should throw exception when rejecting an already COMPLETED request")
    void shouldThrowExceptionWhenRejectingCompletedRequest() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L, 50L);
        request.approve(2L, "Approved");

        DomainException exception = assertThrows(DomainException.class, () ->
            request.reject(2L, "Trying to reject after approval")
        );

        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should record correct referenceType and referenceId when created")
    void shouldRecordCorrectReference() {
        String refType = "STOCK_ADJUSTMENT";
        Long refId = 456L;

        ApprovalRequest request = ApprovalRequest.createNew(refType, refId, 1L, 50L);

        assertEquals(refType, request.getReferenceType());
        assertEquals(refId, request.getReferenceId());
    }

    @Test
    @DisplayName("createNew should produce exactly 1 history entry with REQUESTED action")
    void shouldHaveOneHistoryEntryOnCreate() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, 99L, 50L);

        assertEquals(1, request.getHistories().size());
        assertEquals(ApprovalAction.REQUESTED, request.getHistories().get(0).action());
        assertEquals(99L, request.getHistories().get(0).actorId());
    }
}
