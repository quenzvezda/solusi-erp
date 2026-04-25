package com.solusi.erp.purchasing.purchaserequisition.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPurchaseRequisitionEditViewUseCase Tests")
class GetPurchaseRequisitionEditViewUseCaseTest {

    @Mock
    private PurchaseRequisitionRepository repository;

    private GetPurchaseRequisitionEditViewUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPurchaseRequisitionEditViewUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute returns Optional from repository")
    void execute_returnsOptionalFromRepository() {
        PurchaseRequisition pr = new PurchaseRequisition(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "PR-2607-00001", LocalDate.of(2026, 7, 1), 10L, 2L,
                "IT", PurchaseRequisitionPriority.NORMAL,
                PurchaseRequisitionStatus.DRAFT, "note", true, null, 1L, List.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(pr));

        Optional<PurchaseRequisition> result = useCase.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("PR-2607-00001");
        verify(repository).findById(1L);
    }
}
