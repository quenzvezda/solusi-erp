package com.solusi.erp.common.approval.application.usecase;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.model.ApprovalStatus;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelApprovalRequestUseCaseTest {

    @Mock private ApprovalRequestRepository repository;

    @Test
    void execute_pendingRequest_cancelsAndSaves() {
        ApprovalRequest request = ApprovalRequest.createNew("PURCHASE_RETURN", 1L, "PRT-001", 10L, 50L);
        when(repository.findByReference("PURCHASE_RETURN", 1L)).thenReturn(Optional.of(request));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovalRequest result = new CancelApprovalRequestUseCaseImpl(repository)
                .execute("PURCHASE_RETURN", 1L, 10L, "Cancelled by submitter");

        assertThat(result.getStatus()).isEqualTo(ApprovalStatus.CANCELLED);
        verify(repository).save(request);
    }

    @Test
    void execute_missingRequest_throwsNotFound() {
        when(repository.findByReference("PURCHASE_RETURN", 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CancelApprovalRequestUseCaseImpl(repository)
                .execute("PURCHASE_RETURN", 1L, 10L, "Cancelled by submitter"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> assertThat(((DomainException) ex).getKey())
                        .isEqualTo("msg.error.approval.not-found"));
    }
}
