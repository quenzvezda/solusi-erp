package com.solusi.erp.common.approval.domain.model;

import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ApprovalRequestTest {

    @Test
    @DisplayName("Should create a new ApprovalRequest with PENDING status and initial history")
    void shouldCreateNewApprovalRequest() {
        String refType = "NEWS";
        Long refId = 123L;
        Long requesterId = 1L;

        Long approverId = 50L;

        ApprovalRequest request = ApprovalRequest.createNew(refType, refId, null, requesterId, approverId);

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
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, null, 1L, 50L);
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
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, null, 1L, 50L);
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
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, null, 1L, 50L);
        request.approve(2L, "First Approval");

        DomainException exception = assertThrows(DomainException.class, () -> 
            request.approve(2L, "Second Approval")
        );
        
        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should throw exception when rejecting a non-PENDING request")
    void shouldThrowExceptionWhenRejectingNonPending() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, null, 1L, 50L);
        request.reject(2L, "First Rejection");

        DomainException exception = assertThrows(DomainException.class, () -> 
            request.reject(2L, "Second Rejection")
        );
        
        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should throw exception when approving an already REJECTED request (not-pending)")
    void shouldThrowExceptionWhenApprovingRejectedRequest() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, null, 1L, 50L);
        request.reject(2L, "Rejected notes");

        DomainException exception = assertThrows(DomainException.class, () ->
            request.approve(2L, "Trying to approve after reject")
        );

        assertEquals("msg.error.approval.not-pending", exception.getKey());
    }

    @Test
    @DisplayName("Should throw exception when rejecting an already COMPLETED request")
    void shouldThrowExceptionWhenRejectingCompletedRequest() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 123L, null, 1L, 50L);
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

        ApprovalRequest request = ApprovalRequest.createNew(refType, refId, null, 1L, 50L);

        assertEquals(refType, request.getReferenceType());
        assertEquals(refId, request.getReferenceId());
    }

    @Test
    @DisplayName("createNew should produce exactly 1 history entry with REQUESTED action")
    void shouldHaveOneHistoryEntryOnCreate() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 99L, 50L);

        assertEquals(1, request.getHistories().size());
        assertEquals(ApprovalAction.REQUESTED, request.getHistories().get(0).action());
        assertEquals(99L, request.getHistories().get(0).actorId());
    }

    // --- forward tests ---

    @Test
    @DisplayName("forward with valid input updates currentApproverId, keeps PENDING, records FORWARD history")
    void forward_validInput_updatesCurrentApprover() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 10L, 50L);

        request.forward(50L, 60L, "Forwarding to manager");

        assertThat(request.getCurrentApproverId()).isEqualTo(60L);
        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(request.getHistories()).hasSize(2);

        ApprovalHistory history = request.getHistories().get(1);
        assertThat(history.action()).isEqualTo(ApprovalAction.FORWARD);
        assertThat(history.actorId()).isEqualTo(50L);
        assertThat(history.targetApproverId()).isEqualTo(60L);
        assertThat(history.notes()).isEqualTo("Forwarding to manager");
    }

    @Test
    @DisplayName("forward to self throws cannot-forward-to-self")
    void forward_toSelf_throwsException() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 10L, 50L);

        assertThatThrownBy(() -> request.forward(50L, 50L, "Self forward"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.cannot-forward-to-self"));
    }

    @Test
    @DisplayName("forward with null target throws target-approver-required")
    void forward_nullTarget_throwsException() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 10L, 50L);

        assertThatThrownBy(() -> request.forward(50L, null, "No target"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.target-approver-required"));
    }

    @Test
    @DisplayName("forward with blank notes throws reason-required")
    void forward_blankNotes_throwsException() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 10L, 50L);

        assertThatThrownBy(() -> request.forward(50L, 60L, "   "))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.reason-required"));
    }

    // --- approveAndForward tests ---

    @Test
    @DisplayName("approveAndForward with valid input updates currentApproverId, keeps PENDING")
    void approveAndForward_validInput_updatesCurrentApprover() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 10L, 50L);

        request.approveAndForward(50L, 70L, "Approved, forwarding to director");

        assertThat(request.getCurrentApproverId()).isEqualTo(70L);
        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(request.getHistories()).hasSize(2);

        ApprovalHistory history = request.getHistories().get(1);
        assertThat(history.action()).isEqualTo(ApprovalAction.APPROVE_AND_FORWARD);
        assertThat(history.actorId()).isEqualTo(50L);
        assertThat(history.targetApproverId()).isEqualTo(70L);
    }

    @Test
    @DisplayName("approveAndForward to self throws cannot-forward-to-self")
    void approveAndForward_toSelf_throwsException() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 10L, 50L);

        assertThatThrownBy(() -> request.approveAndForward(50L, 50L, "Self forward"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.cannot-forward-to-self"));
    }

    // --- createNew with approverId ---

    @Test
    @DisplayName("createNew sets currentApproverId correctly")
    void createNew_setsCurrentApproverId() {
        Long approverId = 99L;

        ApprovalRequest request = ApprovalRequest.createNew("STOCK", 200L, null, 1L, approverId);

        assertThat(request.getCurrentApproverId()).isEqualTo(approverId);
        assertThat(request.getStatus()).isEqualTo(ApprovalStatus.PENDING);
    }

    // --- approve with blank notes ---

    @Test
    @DisplayName("approve with blank notes throws reason-required")
    void approve_blankNotes_throwsException() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 1L, null, 10L, 50L);

        assertThatThrownBy(() -> request.approve(50L, ""))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.reason-required"));
    }
}
