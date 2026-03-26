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

        ApprovalRequest request = ApprovalRequest.createNew(refType, refId, requesterId);

        assertNotNull(request);
        assertEquals(refType, request.getReferenceType());
        assertEquals(refId, request.getReferenceId());
        assertEquals(ApprovalStatus.PENDING, request.getStatus());
        assertEquals(1, request.getHistories().size());
        
        ApprovalHistory history = request.getHistories().get(0);
        assertEquals(ApprovalAction.REQUESTED, history.action());
        assertEquals(requesterId, history.actorId());
        assertEquals("Initial Request", history.notes());
    }

    @Test
    @DisplayName("Should change status to COMPLETED when approved")
    void shouldApprove() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L);
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
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L);
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
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L);
        request.approve(2L, "First Approval");

        DomainException exception = assertThrows(DomainException.class, () -> 
            request.approve(2L, "Second Approval")
        );
        
        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should throw exception when rejecting a non-PENDING request")
    void shouldThrowExceptionWhenRejectingNonPending() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, 1L);
        request.reject(2L, "First Rejection");

        DomainException exception = assertThrows(DomainException.class, () -> 
            request.reject(2L, "Second Rejection")
        );
        
        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }
}
