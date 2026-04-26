package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptStatus;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateGoodsReceiptUseCase Tests")
class UpdateGoodsReceiptUseCaseTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    private UpdateGoodsReceiptUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateGoodsReceiptUseCaseImpl(goodsReceiptRepository, purchaseOrderRepository);
    }

    @Test
    void updateDraft_whenRequestedQtyExceedsLatestOutstanding_throws() {
        GoodsReceipt existing = draftReceipt();
        PurchaseOrder po = sentPoWithOutstanding("3.0000");
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.findById(existing.getPoId())).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> useCase.execute(1L, LocalDate.of(2026, 4, 26), "save",
                List.of(new GoodsReceiptLineCommand(null, 101L, 201L, true, new BigDecimal("5.0000"), 1L, 99L, "SN-1,SN-2"))))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.stale.po.changed");
    }

    @Test
    void execute_filtersZeroQuantityCommandsBeforeSaving() {
        GoodsReceipt existing = draftReceipt();
        PurchaseOrder po = sentPoWithOutstanding("3.0000");
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(purchaseOrderRepository.findById(existing.getPoId())).thenReturn(Optional.of(po));
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoodsReceipt result = useCase.execute(
                1L,
                LocalDate.of(2026, 4, 26),
                "updated",
                List.of(
                        new GoodsReceiptLineCommand(null, 101L, 201L, false, BigDecimal.ZERO, 1L, null, null),
                        new GoodsReceiptLineCommand(null, 101L, 201L, false, new BigDecimal("2.0000"), 1L, null, null)
                )
        );

        assertThat(result.getNote()).isEqualTo("updated");
        assertThat(result.getLines()).hasSize(1);
        assertThat(result.getLines().getFirst().getQuantityReceived()).isEqualByComparingTo("2.0000");
        assertThat(result.getLines().getFirst().getBaseQuantity()).isEqualByComparingTo("0");
    }

    private GoodsReceipt draftReceipt() {
        return new GoodsReceipt(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "GR-202604-00001",
                LocalDate.of(2026, 4, 25),
                7L,
                11L,
                3L,
                1L,
                BigDecimal.ONE,
                GoodsReceiptStatus.DRAFT,
                "old",
                List.of(GoodsReceiptLine.prefill(
                        101L,
                        201L,
                        3L,
                        false,
                        new BigDecimal("1.0000"),
                        1L,
                        null,
                        new BigDecimal("150.0000"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        null
                ))
        );
    }

    private PurchaseOrder sentPoWithOutstanding(String outstandingQuantity) {
        BigDecimal quantity = new BigDecimal("5.0000");
        BigDecimal received = quantity.subtract(new BigDecimal(outstandingQuantity));
        PurchaseOrderLine line = PurchaseOrderLine.rehydrate(
                new AuditMetadata(101L, 1L, null, null, null, null),
                7L,
                201L,
                quantity,
                received,
                1L,
                new BigDecimal("150.0000"),
                BigDecimal.ZERO,
                new BigDecimal("750.0000"),
                BigDecimal.ZERO,
                new BigDecimal("750.0000"),
                null,
                null
        );
        return PurchaseOrder.rehydrate(
                new AuditMetadata(7L, 1L, null, null, null, null),
                "PO-0007",
                LocalDate.of(2026, 4, 20),
                LocalDate.of(2026, 4, 25),
                11L,
                3L,
                1L,
                BigDecimal.ONE,
                new BigDecimal("750.0000"),
                BigDecimal.ZERO,
                new BigDecimal("750.0000"),
                PurchaseOrderStatus.SENT,
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
                List.of(line)
        );
    }
}
