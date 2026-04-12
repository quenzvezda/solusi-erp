package com.solusi.erp.purchasing.purchaseorder.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.purchasing.purchaseorder.domain.port.PurchaseOrderEventPublisher;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmitPurchaseOrderUseCase Tests")
class SubmitPurchaseOrderUseCaseTest {

    @Mock
    private PurchaseOrderRepository repository;

    @Mock
    private PurchaseOrderEventPublisher eventPublisher;

    private SubmitPurchaseOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubmitPurchaseOrderUseCaseImpl(repository, eventPublisher);
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

    @Test
    @DisplayName("execute submits draft purchase order with lines and publishes event")
    void execute_submitsDraftWithLines() {
        PurchaseOrder existing = new PurchaseOrder(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "PO-2607-00001", LocalDate.of(2026, 7, 1), null,
                10L, 2L, 3L, BigDecimal.ONE,
                new BigDecimal("1000"), BigDecimal.ZERO, new BigDecimal("1000"),
                PurchaseOrderStatus.DRAFT, 30, null, PurchaseOrderType.DIRECT, "note", true,
                List.of(new PurchaseOrderLine(
                        AuditMetadata.empty(), null, 1L, new BigDecimal("10"),
                        BigDecimal.ZERO, 1L, new BigDecimal("100"), BigDecimal.ZERO,
                        null, null
                ))
        );

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = useCase.execute(1L, 50L);

        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.SUBMITTED);
        verify(repository).save(any(PurchaseOrder.class));
        verify(eventPublisher).publishApprovalRequested(eq(1L), eq("PO-2607-00001"), any(), eq(50L));
    }

    @Test
    @DisplayName("execute throws DomainException when purchase order not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, 50L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.notfound");
    }

    @Test
    @DisplayName("execute throws DomainException when purchase order has no lines")
    void execute_throwsWhenNoLines() {
        PurchaseOrder existing = new PurchaseOrder(
                new AuditMetadata(2L, 1L, null, null, null, null),
                "PO-2607-00002", LocalDate.of(2026, 7, 1), null,
                10L, 2L, 3L, BigDecimal.ONE,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                PurchaseOrderStatus.DRAFT, 30, null, PurchaseOrderType.DIRECT, "note", true,
                List.of()
        );

        when(repository.findById(2L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(2L, 50L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.submit.no.lines");
    }

    @Test
    @DisplayName("execute throws DomainException when purchase order is not DRAFT")
    void execute_throwsWhenNotDraft() {
        PurchaseOrder existing = createDraftPo();
        existing.submit();

        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(1L, 50L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.submit.invalid.status");
    }
}
