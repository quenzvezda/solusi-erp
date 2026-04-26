package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateGoodsReceiptUseCase Tests")
class CreateGoodsReceiptUseCaseTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    private CreateGoodsReceiptUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateGoodsReceiptUseCaseImpl(goodsReceiptRepository, purchaseOrderRepository, sequenceGeneratorService);
    }

    @Test
    void execute_filtersZeroQuantityCommandsBeforeSaving() {
        PurchaseOrder po = sentPoWithOutstanding("3.0000");
        when(purchaseOrderRepository.findById(7L)).thenReturn(Optional.of(po));
        when(sequenceGeneratorService.generate("GOODS_RECEIPT")).thenReturn("GR-202604-00001");
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        GoodsReceipt result = useCase.execute(
                LocalDate.of(2026, 4, 26),
                7L,
                "save",
                List.of(
                        new GoodsReceiptLineCommand(null, 101L, 201L, false, BigDecimal.ZERO, 1L, null, null),
                        new GoodsReceiptLineCommand(null, 101L, 201L, true, new BigDecimal("2.0000"), 1L, 99L, "SN-1,SN-2")
                )
        );

        assertThat(result.getCode()).isEqualTo("GR-202604-00001");
        assertThat(result.getStatus()).isEqualTo(GoodsReceiptStatus.DRAFT);
        assertThat(result.getLines()).hasSize(1);
        assertThat(result.getLines().getFirst().getQuantityReceived()).isEqualByComparingTo("2.0000");
        assertThat(result.getLines().getFirst().getSerialized()).isTrue();
        verify(sequenceGeneratorService).generate("GOODS_RECEIPT");
    }

    @Test
    void execute_whenRequestedQtyExceedsLatestOutstanding_throws() {
        PurchaseOrder po = sentPoWithOutstanding("3.0000");
        when(purchaseOrderRepository.findById(7L)).thenReturn(Optional.of(po));

        assertThatThrownBy(() -> useCase.execute(
                LocalDate.of(2026, 4, 26),
                7L,
                "save",
                List.of(new GoodsReceiptLineCommand(null, 101L, 201L, true, new BigDecimal("5.0000"), 1L, 99L, "SN-1,SN-2"))
        ))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.stale.po.changed");
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
