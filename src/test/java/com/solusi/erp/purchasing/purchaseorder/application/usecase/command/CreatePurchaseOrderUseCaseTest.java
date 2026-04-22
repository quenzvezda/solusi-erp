package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePurchaseOrderUseCase Tests")
class CreatePurchaseOrderUseCaseTest {

    @Mock
    private PurchaseOrderRepository repository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private PurchaseRequisitionRepository purchaseRequisitionRepository;

    private CreatePurchaseOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreatePurchaseOrderUseCaseImpl(repository, sequenceGeneratorService, purchaseRequisitionRepository);
    }

    @Test
    @DisplayName("execute generates code from sequence and saves purchase order")
    void execute_generatesCodeAndSaves() {
        when(sequenceGeneratorService.generate("PO")).thenReturn("PO-2607-00001");
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PoLineInput> lines = List.of(
                new PoLineInput(10L, new BigDecimal("5"), 1L,
                        new BigDecimal("100"), BigDecimal.ZERO, null, "Line 1")
        );

        PurchaseOrder result = useCase.execute(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15),
                1L, 2L, 3L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT, "Test note", lines
        );

        assertThat(result.getCode()).isEqualTo("PO-2607-00001");
        assertThat(result.getSupplierId()).isEqualTo(1L);
        assertThat(result.getFacilityId()).isEqualTo(2L);
        assertThat(result.getCurrencyId()).isEqualTo(3L);
        assertThat(result.getPaymentTermDays()).isEqualTo(30);
        verify(sequenceGeneratorService).generate("PO");
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute creates purchase order with empty lines")
    void execute_createsWithEmptyLines() {
        when(sequenceGeneratorService.generate("PO")).thenReturn("PO-2607-00002");
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = useCase.execute(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15),
                1L, 2L, 3L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT, "note", List.of()
        );

        assertThat(result.getCode()).isEqualTo("PO-2607-00002");
        assertThat(result.getLines()).isEmpty();
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute creates purchase order with multiple lines")
    void execute_createsWithLines() {
        when(sequenceGeneratorService.generate("PO")).thenReturn("PO-2607-00003");
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PoLineInput> lines = List.of(
                new PoLineInput(10L, new BigDecimal("5"), 1L,
                        new BigDecimal("100"), new BigDecimal("0.1"), 100L, "Line 1"),
                new PoLineInput(11L, new BigDecimal("3"), 2L,
                        new BigDecimal("200"), BigDecimal.ZERO, null, null)
        );

        PurchaseOrder result = useCase.execute(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15),
                1L, 2L, 3L, BigDecimal.ONE, 30, 5L, PurchaseOrderType.DIRECT, null, lines
        );

        assertThat(result.getLines()).hasSize(2);
        assertThat(result.getLines().get(0).getProductId()).isEqualTo(10L);
        assertThat(result.getLines().get(1).getQuantity()).isEqualByComparingTo("3");
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute ignores incoming prId when PO type is DIRECT")
    void execute_ignoresIncomingPrIdForDirectPo() {
        when(sequenceGeneratorService.generate("PO")).thenReturn("PO-2607-00004");
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = useCase.execute(
                LocalDate.of(2026, 7, 1), null,
                1L, 2L, 3L, BigDecimal.ONE, 30,
                99L, PurchaseOrderType.DIRECT, "note", List.of()
        );

        assertThat(result.getPoType()).isEqualTo(PurchaseOrderType.DIRECT);
        assertThat(result.getPrId()).isNull();
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute rejects STANDARD PO when prId is missing")
    void execute_rejectsStandardWithoutPrId() {
        assertThatThrownBy(() -> useCase.execute(
                LocalDate.of(2026, 7, 1), null,
                1L, 2L, 3L, BigDecimal.ONE, 30,
                null, PurchaseOrderType.STANDARD, "note", List.of()
        )).isInstanceOf(com.solusi.erp.core.exception.DomainException.class)
                .hasMessage("msg.error.po.standard.pr.required");
    }
}
