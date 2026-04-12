package com.solusi.erp.purchasing.purchaseorder.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPurchaseOrderEditViewUseCase Tests")
class GetPurchaseOrderEditViewUseCaseTest {

    @Mock
    private PurchaseOrderRepository repository;

    private GetPurchaseOrderEditViewUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPurchaseOrderEditViewUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute returns Optional with purchase order when found")
    void execute_returnsWhenFound() {
        PurchaseOrder po = new PurchaseOrder(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "PO-TEST-001", LocalDate.now(), null,
                1L, 1L, 1L, BigDecimal.ONE,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                PurchaseOrderStatus.DRAFT, 30, null, PurchaseOrderType.DIRECT, "test", true,
                List.of()
        );
        when(repository.findById(1L)).thenReturn(Optional.of(po));

        Optional<PurchaseOrder> result = useCase.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("PO-TEST-001");
    }

    @Test
    @DisplayName("execute returns empty Optional when not found")
    void execute_returnsEmptyWhenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<PurchaseOrder> result = useCase.execute(99L);

        assertThat(result).isEmpty();
    }
}
