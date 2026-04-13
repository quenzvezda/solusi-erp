package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CancelPurchaseRequisitionUseCase Tests")
class CancelPurchaseRequisitionUseCaseTest {

    @Mock
    private PurchaseRequisitionRepository repository;

    private CancelPurchaseRequisitionUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelPurchaseRequisitionUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute cancels purchase requisition successfully")
    void execute_cancelsDraftSuccessfully() {
        PurchaseRequisition existing = new PurchaseRequisition(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "PR-2607-00001", LocalDate.of(2026, 7, 1), 10L, 2L,
                "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.DRAFT, "note", true, null, List.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PurchaseRequisition.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseRequisition result = useCase.execute(1L);

        assertThat(result.getStatus()).isEqualTo(PurchaseRequisitionStatus.CANCELLED);
        verify(repository).save(any(PurchaseRequisition.class));
    }

    @Test
    @DisplayName("execute throws DomainException when purchase requisition not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.pr.notfound");
    }
}
