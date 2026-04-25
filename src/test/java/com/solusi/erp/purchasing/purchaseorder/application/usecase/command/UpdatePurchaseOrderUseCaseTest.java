package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisition;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionLine;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionPriority;
import com.solusi.erp.purchasing.purchaserequisition.domain.model.PurchaseRequisitionStatus;
import com.solusi.erp.purchasing.purchaserequisition.domain.repository.PurchaseRequisitionRepository;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdatePurchaseOrderUseCase Tests")
class UpdatePurchaseOrderUseCaseTest {

    @Mock
    private PurchaseOrderRepository repository;

    @Mock
    private PurchaseRequisitionRepository purchaseRequisitionRepository;

    private UpdatePurchaseOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePurchaseOrderUseCaseImpl(repository, purchaseRequisitionRepository);
    }

    private PurchaseOrder createDraftPo() {
        PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null, 1L, new BigDecimal("10"),
                BigDecimal.ZERO, 1L, new BigDecimal("100"), BigDecimal.ZERO,
                null, null
        );
        return PurchaseOrder.createNew("PO-TEST-001", LocalDate.now(), null,
                1L, 1L, 1L, BigDecimal.ONE, 30, null, PurchaseOrderType.DIRECT, "test", List.of(line));
    }

    private PurchaseOrder createDraftStandardPo() {
        PurchaseOrderLine line = new PurchaseOrderLine(
            AuditMetadata.empty(), null, 20L, new BigDecimal("2.0000"),
            BigDecimal.ZERO, 30L, new BigDecimal("750.00"), BigDecimal.ZERO,
            100L, "Derived line"
        );
        return PurchaseOrder.createNew("PO-TEST-STD-001", LocalDate.now(), null,
            100L, 200L, 300L, BigDecimal.ONE, 30, 10L, PurchaseOrderType.STANDARD, "test", List.of(line));
    }

    private PurchaseRequisition approvedPrWithSingleLine() {
        PurchaseRequisitionLine line = new PurchaseRequisitionLine(
            new AuditMetadata(100L, 1L, null, null, null, null),
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
            new AuditMetadata(10L, 1L, null, null, null, null),
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
    @DisplayName("execute updates draft purchase order and saves")
    void execute_updatesDraftPo() {
        PurchaseOrder existing = createDraftPo();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PoLineInput> newLines = List.of(
                new PoLineInput(2L, new BigDecimal("20"), 2L,
                        new BigDecimal("200"), BigDecimal.ZERO, null, "Updated line")
        );

        PurchaseOrder result = useCase.execute(1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15),
                5L, 6L, new BigDecimal("2"), 45, "Updated note", newLines
        );

        assertThat(result.getNote()).isEqualTo("Updated note");
        assertThat(result.getFacilityId()).isEqualTo(5L);
        assertThat(result.getPaymentTermDays()).isEqualTo(45);
        assertThat(result.getLines()).hasSize(1);
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute throws DomainException when purchase order not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L,
                LocalDate.now(), null, 1L, 1L, BigDecimal.ONE, 30, null, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.notfound");
    }

    @Test
    @DisplayName("execute throws DomainException when purchase order is not DRAFT")
    void execute_throwsWhenNotDraft() {
        PurchaseOrder existing = createDraftPo();
        existing.submit();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        List<PoLineInput> lines = List.of(
                new PoLineInput(1L, new BigDecimal("1.0000"), 1L,
                        new BigDecimal("100.00"), BigDecimal.ZERO, null, "Line")
        );

        assertThatThrownBy(() -> useCase.execute(1L,
                LocalDate.now(), null, 1L, 1L, BigDecimal.ONE, 30, null, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.update.not.draft");
    }

    @Test
    @DisplayName("execute rejects purchase order update when lines are empty")
    void execute_rejectsUpdateWhenLinesAreEmpty() {
        PurchaseOrder existing = createDraftPo();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(
                1L,
                LocalDate.now(), null, 1L, 1L, BigDecimal.ONE, 30, null, List.of()
        )).isInstanceOf(DomainException.class)
                .hasMessage("msg.error.po.save.no.lines");
    }

    @Test
    @DisplayName("execute rejects STANDARD draft when updated header no longer matches referenced PR")
    void execute_rejectsStandardDraftWhenHeaderNoLongerMatchesReferencedPr() {
        PurchaseOrder existing = createDraftStandardPo();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(purchaseRequisitionRepository.findById(10L)).thenReturn(Optional.of(approvedPrWithSingleLine()));

        assertThatThrownBy(() -> useCase.execute(
            1L,
            LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15),
            999L, 300L, BigDecimal.ONE, 45, "Updated note",
            List.of(new PoLineInput(20L, new BigDecimal("2.0000"), 30L,
                new BigDecimal("750.00"), BigDecimal.ZERO, 100L, "Updated line"))
        )).isInstanceOf(DomainException.class)
            .hasMessage("msg.error.po.standard.header.mismatch");
    }

    @Test
    @DisplayName("execute updates header tax snapshot and recalculates totals")
    void execute_updatesHeaderTaxSnapshotAndTotals() {
        PurchaseOrder existing = createDraftPo();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        List<PoLineInput> newLines = List.of(
                new PoLineInput(2L, new BigDecimal("2"), 2L,
                        new BigDecimal("100.00"), BigDecimal.ZERO, null, "Updated line")
        );

        PurchaseOrder result = useCase.execute(
                1L,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 15),
                5L, 6L, new BigDecimal("2"), 45,
                11L, "PPN-EX", "PPN 11% Exclusive", new BigDecimal("11.00"),
                TaxCalculationMode.EXCLUSIVE,
                "Updated note", newLines
        );

        assertThat(result.getTaxId()).isEqualTo(11L);
        assertThat(result.getTaxCode()).isEqualTo("PPN-EX");
        assertThat(result.getTaxCalculationMode()).isEqualTo(TaxCalculationMode.EXCLUSIVE);
        assertThat(result.getSubtotal()).isEqualByComparingTo("200.0000");
        assertThat(result.getTaxAmount()).isEqualByComparingTo("22.0000");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("222.0000");
    }
}
