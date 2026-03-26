package com.solusi.erp.common.approval.application.usecase;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateApprovalRequestUseCaseImplTest {

    @Mock
    private ApprovalRequestRepository repository;

    private CreateApprovalRequestUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateApprovalRequestUseCaseImpl(repository);
    }

    @Test
    @DisplayName("Should create a new approval request and save it")
    void shouldCreateAndSave() {
        String refType = "NEWS";
        Long refId = 123L;
        String requesterUsername = "admin";

        when(repository.save(any(ApprovalRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = useCase.execute(refType, refId, requesterUsername);

        assertNotNull(result);
        assertEquals(refType, result.getReferenceType());
        assertEquals(refId, result.getReferenceId());
        assertEquals(ApprovalStatus.PENDING, result.getStatus());
        
        ArgumentCaptor<ApprovalRequest> captor = ArgumentCaptor.forClass(ApprovalRequest.class);
        verify(repository).save(captor.capture());
        
        ApprovalRequest saved = captor.getValue();
        assertEquals(refType, saved.getReferenceType());
        assertEquals(refId, saved.getReferenceId());
    }
}
