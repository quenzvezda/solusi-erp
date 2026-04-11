package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
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

    private UpdatePurchaseOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdatePurchaseOrderUseCaseImpl(repository);
    }

    private PurchaseOrder createDraftPo() {
        PurchaseOrderLine line = new PurchaseOrderLine(
                AuditMetadata.empty(), null, 1L, new BigDecimal("10"),
                BigDecimal.ZERO, 1L, new BigDecimal("100"), BigDecimal.ZERO,
                null, null
        );
        return PurchaseOrder.createNew("PO-TEST-001", LocalDate.now(), null,
                1L, 1L, 1L, BigDecimal.ONE, 30, null, "test", List.of(line));
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

        assertThatThrownBy(() -> useCase.execute(1L,
                LocalDate.now(), null, 1L, 1L, BigDecimal.ONE, 30, null, List.of()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.update.not.draft");
    }
}
