package com.solusi.erp.common.approval.application.usecase.query;

import com.solusi.erp.common.approval.domain.model.ApprovalRequest;
import com.solusi.erp.common.approval.domain.repository.ApprovalRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindApprovalRequestByReferenceUseCaseImplTest {

    @Mock
    private ApprovalRequestRepository repository;

    private FindApprovalRequestByReferenceUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindApprovalRequestByReferenceUseCaseImpl(repository);
    }

    @Test
    @DisplayName("Should throw NullPointerException when repository is null in constructor")
    void shouldFailWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new FindApprovalRequestByReferenceUseCaseImpl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("repository");
    }

    @Test
    @DisplayName("Should throw NullPointerException when referenceType is null")
    void shouldFailWhenReferenceTypeIsNull() {
        assertThatThrownBy(() -> useCase.execute(null, 123L))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("referenceType");
    }

    @Test
    @DisplayName("Should throw NullPointerException when referenceId is null")
    void shouldFailWhenReferenceIdIsNull() {
        assertThatThrownBy(() -> useCase.execute("NEWS", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("referenceId");
    }

    @Test
    @DisplayName("Should find approval request by reference when valid parameters provided")
    void shouldFindByReferenceWhenValid() {
        String referenceType = "NEWS";
        Long referenceId = 123L;
        ApprovalRequest mockRequest = ApprovalRequest.createNew(referenceType, referenceId, null, 1L, 50L);
        
        when(repository.findByReference(referenceType, referenceId))
                .thenReturn(Optional.of(mockRequest));

        Optional<ApprovalRequest> result = useCase.execute(referenceType, referenceId);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(mockRequest);
        verify(repository).findByReference(referenceType, referenceId);
    }

    @Test
    @DisplayName("Should return empty when approval request not found")
    void shouldReturnEmptyWhenNotFound() {
        String referenceType = "NEWS";
        Long referenceId = 999L;
        
        when(repository.findByReference(referenceType, referenceId))
                .thenReturn(Optional.empty());

        Optional<ApprovalRequest> result = useCase.execute(referenceType, referenceId);

        assertThat(result).isEmpty();
        verify(repository).findByReference(referenceType, referenceId);
    }
}
