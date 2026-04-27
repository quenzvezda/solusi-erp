package com.solusi.erp.inventory.goodsreceipt.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptReferenceType;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptStatus;
import com.solusi.erp.inventory.goodsreceipt.domain.port.GoodsReceiptSourceResolver;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.adapter.PurchaseOrderGoodsReceiptSourceResolver;
import com.solusi.erp.inventory.goodsreceipt.infrastructure.service.GoodsReceiptSourceResolverRegistry;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetGoodsReceiptCreateViewUseCase Tests")
class GetGoodsReceiptCreateViewUseCaseTest {

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    private GetGoodsReceiptCreateViewUseCase useCase;

    @BeforeEach
    void setUp() {
        GoodsReceiptSourceResolverRegistry registry = new GoodsReceiptSourceResolverRegistry(
                List.of(new PurchaseOrderGoodsReceiptSourceResolver(purchaseOrderRepository))
        );
        useCase = new GetGoodsReceiptCreateViewUseCaseImpl(registry);
    }

    @Test
    void buildDraftFromReference_prefillsOutstandingLinesWithZeroQty() {
        PurchaseOrder po = sentPoWithOutstandingLines();
        when(purchaseOrderRepository.findById(7L)).thenReturn(Optional.of(po));

        GoodsReceipt draft = useCase.execute(GoodsReceiptReferenceType.PURCHASE_ORDER, 7L);

        assertThat(draft.getReferenceType()).isEqualTo(GoodsReceiptReferenceType.PURCHASE_ORDER);
        assertThat(draft.getReferenceId()).isEqualTo(7L);
        assertThat(draft.getSupplierId()).isEqualTo(11L);
        assertThat(draft.getFacilityId()).isEqualTo(3L);
        assertThat(draft.getStatus()).isEqualTo(GoodsReceiptStatus.DRAFT);
        assertThat(draft.getLines()).hasSize(2);
        assertThat(draft.getLines()).extracting(GoodsReceiptLine::getReferenceLineId).containsExactly(101L, 102L);
        assertThat(draft.getLines()).allMatch(line -> line.getQuantityReceived().compareTo(BigDecimal.ZERO) == 0);
        assertThat(draft.getLines()).allMatch(line -> Boolean.FALSE.equals(line.getSerialized()));
    }

    @Test
    void buildDraftFromReference_whenPoNotReceivable_throws() {
        PurchaseOrder po = purchaseOrderWithStatus(PurchaseOrderStatus.DRAFT, List.of(poLine(101L, 201L, "5.0000", "0.0000")));
        when(purchaseOrderRepository.findById(7L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> useCase.execute(GoodsReceiptReferenceType.PURCHASE_ORDER, 7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.po.invalid.status");
    }

    @Test
    void buildDraftFromReference_whenReferenceTypeNotSupported_throws() {
        assertThatThrownBy(() -> useCase.execute(GoodsReceiptReferenceType.PRODUCTION, 7L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.reference.unsupported");
    }

    @Test
    void registry_whenDuplicateReferenceTypeRegistered_throws() {
        assertThatThrownBy(() -> new GoodsReceiptSourceResolverRegistry(List.of(
                resolver(GoodsReceiptReferenceType.PURCHASE_ORDER),
                resolver(GoodsReceiptReferenceType.PURCHASE_ORDER)
        )))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.common.duplicate");
    }

    private PurchaseOrder sentPoWithOutstandingLines() {
        return purchaseOrderWithStatus(PurchaseOrderStatus.SENT, List.of(
                poLine(101L, 201L, "5.0000", "0.0000"),
                poLine(102L, 202L, "8.0000", "6.0000"),
                poLine(103L, 203L, "4.0000", "4.0000")
        ));
    }

    private PurchaseOrder purchaseOrderWithStatus(PurchaseOrderStatus status, List<PurchaseOrderLine> lines) {
        return PurchaseOrder.rehydrate(
                new AuditMetadata(7L, 1L, null, null, null, null),
                "PO-0007",
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 25),
                11L,
                3L,
                1L,
                BigDecimal.ONE,
                new BigDecimal("1000.0000"),
                BigDecimal.ZERO,
                new BigDecimal("1000.0000"),
                status,
                14,
                null,
                PurchaseOrderType.DIRECT,
                null,
                null,
                null,
                BigDecimal.ZERO,
                null,
                null,
                true,
                lines
        );
    }

    private PurchaseOrderLine poLine(Long id, Long productId, String quantity, String receivedQuantity) {
        return PurchaseOrderLine.rehydrate(
                new AuditMetadata(id, 1L, null, null, null, null),
                7L,
                productId,
                new BigDecimal(quantity),
                new BigDecimal(receivedQuantity),
                1L,
                new BigDecimal("150.0000"),
                BigDecimal.ZERO,
                new BigDecimal("750.0000"),
                BigDecimal.ZERO,
                new BigDecimal("750.0000"),
                null,
                null
        );
    }

    private GoodsReceiptSourceResolver resolver(GoodsReceiptReferenceType referenceType) {
        return new GoodsReceiptSourceResolver() {
            @Override
            public GoodsReceiptReferenceType getReferenceType() {
                return referenceType;
            }

            @Override
            public GoodsReceipt resolve(Long referenceId) {
                return null;
            }
        };
    }
}
