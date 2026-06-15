package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessApprovalUseCaseImplTest {

    @Mock
    private ApprovalRequestRepository repository;

    @Mock
    private ApprovalEventPublisher eventPublisher;

    private ProcessApprovalUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessApprovalUseCaseImpl(repository, eventPublisher);
    }

    @Test
    @DisplayName("Should approve and publish completed event")
    void shouldApproveAndPublishEvent() {
        Long requestId = 1L;
        Long actorId = 2L;
        String notes = "OK";
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, null, 1L, 50L);

        when(repository.findById(requestId)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.approve(requestId, actorId, notes);

        assertNotNull(result);
        assertEquals(ApprovalStatus.COMPLETED, result.getStatus());
        verify(repository).save(request);
        verify(eventPublisher).publishCompleted("NEWS", 100L, actorId);
    }

    @Test
    @DisplayName("Should reject and publish rejected event")
    void shouldRejectAndPublishEvent() {
        Long requestId = 1L;
        Long actorId = 2L;
        String notes = "Bad";
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, null, 1L, 50L);

        when(repository.findById(requestId)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.reject(requestId, actorId, notes);

        assertNotNull(result);
        assertEquals(ApprovalStatus.REJECTED, result.getStatus());
        verify(repository).save(request);
        verify(eventPublisher).publishRejected("NEWS", 100L);
    }

    @Test
    @DisplayName("Should throw exception when request not found")
    void shouldThrowExceptionWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () -> 
            useCase.approve(1L, 2L, "Notes")
        );

        assertEquals("msg.error.approval.not-found", exception.getKey());
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishCompleted(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw DomainException when trying to approve an already COMPLETED request")
    void shouldThrowWhenApprovingAlreadyCompleted() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, null, 1L, 50L);
        request.approve(2L, "First approval");

        when(repository.findById(1L)).thenReturn(Optional.of(request));

        DomainException exception = assertThrows(DomainException.class, () ->
            useCase.approve(1L, 3L, "Second attempt")
        );

        assertEquals("msg.error.approval.not-pending", exception.getKey());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DomainException when trying to reject an already REJECTED request")
    void shouldThrowWhenRejectingAlreadyRejected() {
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, null, 1L, 50L);
        request.reject(2L, "Already rejected");

        when(repository.findById(1L)).thenReturn(Optional.of(request));

        DomainException exception = assertThrows(DomainException.class, () ->
            useCase.reject(1L, 3L, "Double reject")
        );

        assertEquals("msg.error.approval.not-pending", exception.getKey());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception for reject if request not found")
    void shouldThrowExceptionWhenRejectingNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        DomainException exception = assertThrows(DomainException.class, () ->
            useCase.reject(99L, 2L, "Notes")
        );

        assertEquals("msg.error.approval.not-found", exception.getKey());
        verify(repository, never()).save(any());
        verify(eventPublisher, never()).publishRejected(any(), any());
    }

    // --- forward tests ---

    @Test
    @DisplayName("forward delegates to domain and saves")
    void forward_validInput_delegatesToDomain() {
        Long requestId = 1L;
        Long actorId = 2L;
        Long targetApproverId = 3L;
        String notes = "Please review";
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, null, 1L, actorId);

        when(repository.findById(requestId)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.forward(requestId, actorId, targetApproverId, notes);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(result.getCurrentApproverId()).isEqualTo(targetApproverId);
        verify(repository).save(request);
        verify(eventPublisher, never()).publishCompleted(any(), any(), any());
        verify(eventPublisher, never()).publishRejected(any(), any());
    }

    @Test
    @DisplayName("forward throws when request not found")
    void forward_notFound_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.forward(99L, 2L, 3L, "Notes"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.not-found"));

        verify(repository, never()).save(any());
    }

    // --- approveAndForward tests ---

    @Test
    @DisplayName("approveAndForward delegates to domain and saves")
    void approveAndForward_validInput_delegatesToDomain() {
        Long requestId = 1L;
        Long actorId = 2L;
        Long targetApproverId = 3L;
        String notes = "Approved, next level";
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, null, 1L, actorId);

        when(repository.findById(requestId)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.approveAndForward(requestId, actorId, targetApproverId, notes);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(result.getCurrentApproverId()).isEqualTo(targetApproverId);
        verify(repository).save(request);
        verify(eventPublisher, never()).publishCompleted(any(), any(), any());
    }

    @Test
    @DisplayName("approveAndForward throws when request not found")
    void approveAndForward_notFound_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.approveAndForward(99L, 2L, 3L, "Notes"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.not-found"));

        verify(repository, never()).save(any());
    }
}
