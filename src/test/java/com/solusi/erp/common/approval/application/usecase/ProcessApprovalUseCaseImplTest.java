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
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, 1L);

        when(repository.findById(requestId)).thenReturn(Optional.of(request));
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.approve(requestId, actorId, notes);

        assertNotNull(result);
        assertEquals(ApprovalStatus.COMPLETED, result.getStatus());
        verify(repository).save(request);
        verify(eventPublisher).publishCompleted("NEWS", 100L);
    }

    @Test
    @DisplayName("Should reject and publish rejected event")
    void shouldRejectAndPublishEvent() {
        Long requestId = 1L;
        Long actorId = 2L;
        String notes = "Bad";
        ApprovalRequest request = ApprovalRequest.createNew("NEWS", 100L, 1L);

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
        verify(eventPublisher, never()).publishCompleted(any(), any());
    }
}
