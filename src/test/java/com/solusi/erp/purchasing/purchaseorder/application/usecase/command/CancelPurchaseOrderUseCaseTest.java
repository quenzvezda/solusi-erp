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
@DisplayName("CancelPurchaseOrderUseCase Tests")
class CancelPurchaseOrderUseCaseTest {

    @Mock
    private PurchaseOrderRepository repository;

    private CancelPurchaseOrderUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelPurchaseOrderUseCaseImpl(repository);
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
    @DisplayName("execute cancels draft purchase order")
    void execute_cancelsDraftPo() {
        PurchaseOrder existing = createDraftPo();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = useCase.execute(1L);

        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute cancels submitted purchase order")
    void execute_cancelsSubmittedPo() {
        PurchaseOrder existing = createDraftPo();
        existing.submit();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(PurchaseOrder.class))).thenAnswer(inv -> inv.getArgument(0));

        PurchaseOrder result = useCase.execute(1L);

        assertThat(result.getStatus()).isEqualTo(PurchaseOrderStatus.CANCELLED);
        verify(repository).save(any(PurchaseOrder.class));
    }

    @Test
    @DisplayName("execute throws DomainException when purchase order not found")
    void execute_throwsWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.notfound");
    }

    @Test
    @DisplayName("execute throws DomainException when purchase order is APPROVED")
    void execute_throwsWhenApproved() {
        PurchaseOrder existing = createDraftPo();
        existing.submit();
        existing.approve();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> useCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.po.cancel.invalid.status");
    }
}
