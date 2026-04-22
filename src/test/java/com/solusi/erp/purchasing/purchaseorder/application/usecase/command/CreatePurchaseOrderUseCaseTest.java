package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
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
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

    private PurchaseRequisition approvedPrWithSingleLine() {
        PurchaseRequisitionLine line = new PurchaseRequisitionLine(
            new com.solusi.erp.core.domain.model.AuditMetadata(100L, 1L, null, null, null, null),
            10L,
            20L,
            new BigDecimal("5.0000"),
            30L,
            LocalDate.of(2026, 7, 20),
            new BigDecimal("750.0000"),
            null,
            "PR line"
        );

        return new PurchaseRequisition(
            new com.solusi.erp.core.domain.model.AuditMetadata(10L, 1L, null, null, null, null),
            "PR-001",
            LocalDate.of(2026, 7, 1),
            99L,
            200L,
            "IT",
            PurchaseRequisitionPriority.NORMAL,
            PurchaseRequisitionStatus.APPROVED,
            "Approved PR",
            true,
            100L,
            300L,
            List.of(line)
        );
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
    @DisplayName("execute rejects purchase order save when lines are empty")
    void execute_rejectsSaveWhenLinesAreEmpty() {
        assertThatThrownBy(() -> useCase.execute(
                LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 15),
                1L, 2L, 3L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT, "note", List.of()
        )).isInstanceOf(com.solusi.erp.core.exception.DomainException.class)
                .hasMessage("msg.error.po.save.no.lines");
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

        List<PoLineInput> lines = List.of(
                new PoLineInput(10L, new BigDecimal("1.0000"), 1L,
                        new BigDecimal("100.00"), BigDecimal.ZERO, null, "Direct line")
        );

        PurchaseOrder result = useCase.execute(
                LocalDate.of(2026, 7, 1), null,
                1L, 2L, 3L, BigDecimal.ONE, 30,
                99L, PurchaseOrderType.DIRECT, "note", lines
        );

        assertThat(result.getPoType()).isEqualTo(PurchaseOrderType.DIRECT);
        assertThat(result.getPrId()).isNull();
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute rejects STANDARD PO when prId is missing")
    void execute_rejectsStandardWithoutPrId() {
        List<PoLineInput> lines = List.of(
                new PoLineInput(20L, new BigDecimal("1.0000"), 30L,
                        new BigDecimal("750.00"), BigDecimal.ZERO, 100L, null)
        );

        assertThatThrownBy(() -> useCase.execute(
                LocalDate.of(2026, 7, 1), null,
                1L, 2L, 3L, BigDecimal.ONE, 30,
                null, PurchaseOrderType.STANDARD, "note", lines
        )).isInstanceOf(com.solusi.erp.core.exception.DomainException.class)
                .hasMessage("msg.error.po.standard.pr.required");
    }

    @Test
    @DisplayName("execute rejects STANDARD PO when a line is missing prLineId")
    void execute_rejectsStandardLineWithoutPrLineId() {
        when(purchaseRequisitionRepository.findById(10L)).thenReturn(Optional.of(approvedPrWithSingleLine()));

        assertThatThrownBy(() -> useCase.execute(
            LocalDate.of(2026, 7, 1), null,
            100L, 200L, 300L, BigDecimal.ONE, 30,
            10L, PurchaseOrderType.STANDARD, "note",
            List.of(new PoLineInput(20L, new BigDecimal("1.0000"), 30L,
                new BigDecimal("750.00"), BigDecimal.ZERO, null, null))
        )).isInstanceOf(com.solusi.erp.core.exception.DomainException.class)
            .hasMessage("msg.error.po.standard.line.prLine.required");
    }

    @Test
    @DisplayName("execute rejects STANDARD PO when header does not match referenced PR")
    void execute_rejectsStandardWhenHeaderDoesNotMatchReferencedPr() {
        when(purchaseRequisitionRepository.findById(10L)).thenReturn(Optional.of(approvedPrWithSingleLine()));

        assertThatThrownBy(() -> useCase.execute(
            LocalDate.of(2026, 7, 1), null,
            999L, 200L, 300L, BigDecimal.ONE, 30,
            10L, PurchaseOrderType.STANDARD, "note",
            List.of(new PoLineInput(20L, new BigDecimal("1.0000"), 30L,
                new BigDecimal("750.00"), BigDecimal.ZERO, 100L, null))
        )).isInstanceOf(com.solusi.erp.core.exception.DomainException.class)
            .hasMessage("msg.error.po.standard.header.mismatch");
    }

    @Test
    @DisplayName("execute rejects STANDARD PO when duplicate prLineId appears in one request")
    void execute_rejectsStandardWhenDuplicatePrLineIdAppears() {
        when(purchaseRequisitionRepository.findById(10L)).thenReturn(Optional.of(approvedPrWithSingleLine()));

        assertThatThrownBy(() -> useCase.execute(
            LocalDate.of(2026, 7, 1), null,
            100L, 200L, 300L, BigDecimal.ONE, 30,
            10L, PurchaseOrderType.STANDARD, "note",
            List.of(
                new PoLineInput(20L, new BigDecimal("1.0000"), 30L,
                    new BigDecimal("750.00"), BigDecimal.ZERO, 100L, null),
                new PoLineInput(20L, new BigDecimal("1.0000"), 30L,
                    new BigDecimal("750.00"), BigDecimal.ZERO, 100L, null)
            )
        )).isInstanceOf(com.solusi.erp.core.exception.DomainException.class)
            .hasMessage("msg.error.po.standard.line.duplicate");
    }

    @Test
    @DisplayName("execute rejects STANDARD PO when requested quantity exceeds remaining quantity")
    void execute_rejectsStandardWhenRequestedQuantityExceedsRemainingQuantity() {
        when(purchaseRequisitionRepository.findById(10L)).thenReturn(Optional.of(approvedPrWithSingleLine()));
        when(repository.sumCommittedQuantityByPrLineIds(Set.of(100L)))
            .thenReturn(Map.of(100L, new BigDecimal("4.0000")));

        assertThatThrownBy(() -> useCase.execute(
            LocalDate.of(2026, 7, 1), null,
            100L, 200L, 300L, BigDecimal.ONE, 30,
            10L, PurchaseOrderType.STANDARD, "note",
            List.of(new PoLineInput(20L, new BigDecimal("2.0000"), 30L,
                new BigDecimal("750.00"), BigDecimal.ZERO, 100L, null))
        )).isInstanceOf(com.solusi.erp.core.exception.DomainException.class)
            .hasMessage("msg.error.po.standard.line.quantity.exceeds.remaining");
    }
}
