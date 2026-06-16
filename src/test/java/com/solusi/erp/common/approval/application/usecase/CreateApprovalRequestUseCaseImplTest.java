package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.application.port.ApprovalEventPublisher;
import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class CreateApprovalRequestUseCaseImplTest {

    @Mock
    private ApprovalRequestRepository repository;

    @Mock
    private ApprovalEventPublisher eventPublisher;

    private CreateApprovalRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateApprovalRequestUseCaseImpl(repository, eventPublisher);
    }

    @Test
    @DisplayName("Should create a new approval request and save it")
    void shouldCreateAndSave() {
        String refType = "NEWS";
        Long refId = 123L;
        Long requesterId = 11L;

        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.execute(refType, refId, null, "/common/news/view/123", requesterId, null);

        assertNotNull(result);
        assertEquals(refType, result.getReferenceType());
        assertEquals(refId, result.getReferenceId());
        assertEquals(ApprovalStatus.PENDING, result.getStatus());
        
        ArgumentCaptor<ApprovalRequest> captor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(repository).save(captor.capture());
        
        ApprovalRequest saved = captor.getValue();
        assertEquals(refType, saved.getReferenceType());
        assertEquals(refId, saved.getReferenceId());
        assertEquals("/common/news/view/123", saved.getDocumentPath());
        verify(eventPublisher).publishRequested(saved);
    }

    @Test
    @DisplayName("Should create request for unknown referenceType without validation error (flexible polymorphic design)")
    void shouldCreateForUnknownReferenceType() {
        String refType = "FUTURE_MODULE";
        Long refId = 999L;

        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.execute(refType, refId, null, "/future-module/999", 1L, null);

        assertNotNull(result);
        assertEquals(refType, result.getReferenceType());
        assertEquals(refId, result.getReferenceId());
        assertEquals(ApprovalStatus.PENDING, result.getStatus());
        verify(repository).save(any(ApprovalRequest.class));
        verify(eventPublisher).publishRequested(result);
    }

    @Test
    @DisplayName("Should always set initial status to PENDING")
    void shouldAlwaysStartWithPendingStatus() {
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.execute("NEWS", 1L, null, "/common/news/view/1", 1L, null);

        assertEquals(ApprovalStatus.PENDING, result.getStatus());
    }

    @Test
    @DisplayName("Should call repository.save exactly once")
    void shouldCallSaveExactlyOnce() {
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("NEWS", 1L, null, "/common/news/view/1", 1L, null);

        verify(repository, times(1)).save(any(ApprovalRequest.class));
        verify(eventPublisher, times(1)).publishRequested(any(ApprovalRequest.class));
    }

    @Test
    @DisplayName("Should create request with approverId setting currentApprover")
    void execute_withApproverId_setsCurrentApprover() {
        Long approverId = 42L;
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.execute("STOCK", 200L, null, "/inventory/stock-adjustments/view/200", 1L, approverId);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentApproverId()).isEqualTo(approverId);
        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.PENDING);
        assertThat(result.getReferenceType()).isEqualTo("STOCK");
        assertThat(result.getReferenceId()).isEqualTo(200L);

        ArgumentCaptor<ApprovalRequest> captor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCurrentApproverId()).isEqualTo(approverId);
        verify(eventPublisher).publishRequested(captor.getValue());
    }

    @Test
    @DisplayName("Should publish requested event after save")
    void execute_shouldPublishRequestedAfterSave() {
        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.execute("NEWS", 1L, "NEWS-1", "/common/news/view/1", 1L, 2L);

        var inOrder = inOrder(repository, eventPublisher);
        inOrder.verify(repository).save(any(ApprovalRequest.class));
        inOrder.verify(eventPublisher).publishRequested(result);
    }
}
