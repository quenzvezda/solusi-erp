package com.solusi.erp.purchasing.purchaserequisition.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePurchaseRequisitionUseCase Tests")
class CreatePurchaseRequisitionUseCaseTest {

    @Mock
    private PurchaseRequisitionRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreatePurchaseRequisitionUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreatePurchaseRequisitionUseCaseImpl(repository, sequenceGeneratorService);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves purchase requisition")
    void execute_generatesCodeAndSaves() {
        when(sequenceGeneratorService.generate("PR")).thenReturn("PR-2607-00001");
        when(repository.save(any(PurchaseRequisition.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseRequisition result = useCase.execute(
                LocalDate.of(2026, 7, 1), 1L, 2L,
                "IT", PurchaseRequisitionPriority.NORMAL,
                "Test note", List.of()
        );

        assertThat(result.getCode()).isEqualTo("PR-2607-00001");
        assertThat(result.getRequesterId()).isEqualTo(1L);
        assertThat(result.getFacilityId()).isEqualTo(2L);
        assertThat(result.getDepartment()).isEqualTo("IT");
        verify(sequenceGeneratorService).generate("PR");
        verify(repository).save(any(PurchaseRequisition.class));
    }

    @Test
    @DisplayName("execute creates purchase requisition with lines")
    void execute_createsWithLines() {
        when(sequenceGeneratorService.generate("PR")).thenReturn("PR-2607-00002");
        when(repository.save(any(PurchaseRequisition.class))).thenAnswer(inv -> inv.getArgument(0));

        List<LineInput> lines = List.of(
                new LineInput(10L, new BigDecimal("5.0000"), 1L,
                        LocalDate.of(2026, 7, 15), new BigDecimal("100.0000"), 20L, "Line 1"),
                new LineInput(11L, new BigDecimal("3.0000"), 2L,
                        LocalDate.of(2026, 7, 20), new BigDecimal("200.0000"), null, null)
        );

        PurchaseRequisition result = useCase.execute(
                LocalDate.of(2026, 7, 1), 1L, 2L,
                "HR", PurchaseRequisitionPriority.HIGH,
                null, lines
        );

        assertThat(result.getLines()).hasSize(2);
        assertThat(result.getLines().get(0).getProductId()).isEqualTo(10L);
        assertThat(result.getLines().get(1).getQuantity()).isEqualByComparingTo("3.0000");
        verify(repository).save(any(PurchaseRequisition.class));
    }
}
