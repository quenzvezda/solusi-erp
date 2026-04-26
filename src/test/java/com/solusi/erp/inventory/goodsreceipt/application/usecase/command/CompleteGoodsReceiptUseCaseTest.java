package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptStatus;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderStatus;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderType;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class CompleteGoodsReceiptUseCaseTest {

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    @Mock
    private StockService stockService;

    private CompleteGoodsReceiptUseCase completeUseCase;

    @BeforeEach
    void setUp() {
        completeUseCase = new CompleteGoodsReceiptUseCaseImpl(
                goodsReceiptRepository,
                purchaseOrderRepository,
                ensureOpenPeriodForDateUseCase,
                stockService
        );
    }

    @Test
    void complete_callsStockServiceAndUpdatesPurchaseOrder() {
        GoodsReceipt receipt = draftReceiptWithOneActiveLine();
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));

        completeUseCase.execute(1L);

        verify(ensureOpenPeriodForDateUseCase).execute(receipt.getReceiptDate());
        verify(stockService).adjust(argThat(payload ->
                payload.getReferenceType() == ReferenceType.GOODS_RECEIPT &&
                        payload.getUomId().equals(1L) &&
                        payload.getExchangeRate().compareTo(BigDecimal.ONE) == 0 &&
                        payload.getNetPrice().compareTo(new BigDecimal("150.0000")) == 0));
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
        verify(goodsReceiptRepository).save(argThat(saved ->
                saved.getStatus() == GoodsReceiptStatus.COMPLETED));
    }

    @Test
    void complete_usesPurchaseOrderExchangeRateForStockValuation() {
        GoodsReceipt receipt = receiptWithLine(false, new BigDecimal("3.0000"), BigDecimal.ONE, null);
        PurchaseOrder po = sentPoWithOutstanding("10.0000", new BigDecimal("15000"));
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(po));

        completeUseCase.execute(1L);

        verify(stockService).adjust(argThat(payload ->
                payload.getExchangeRate().compareTo(new BigDecimal("15000")) == 0));
    }

    @Test
    void complete_whenRequestedQtyExceedsLatestOutstanding_throws() {
        GoodsReceipt receipt = draftReceiptWithOneActiveLine();
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("2.0000")));

        assertThatThrownBy(() -> completeUseCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.stale.po.changed");
    }

    @Test
    void complete_whenSerializedQtyHasFraction_throws() {
        GoodsReceipt receipt = receiptWithLine(true, new BigDecimal("1.5000"), BigDecimal.ONE, null);
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));

        assertThatThrownBy(() -> completeUseCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.serial.quantity.whole");
    }

    @Test
    void complete_serializedLineSplitsTransactionQuantityAcrossSerials() {
        GoodsReceipt receipt = receiptWithLine(true, new BigDecimal("1.0000"), BigDecimal.ONE, "SN-1,SN-2");
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));

        completeUseCase.execute(1L);

        verify(stockService, times(2)).adjust(argThat((StockMovementPayload payload) ->
                payload.getQuantity().compareTo(new BigDecimal("0.500000")) == 0
                        && payload.getSerialNumber() != null
                        && payload.getUomId().equals(1L)));
    }

    private GoodsReceipt draftReceiptWithOneActiveLine() {
        return receiptWithLine(false, new BigDecimal("3.0000"), BigDecimal.ONE, null);
    }

    private GoodsReceipt receiptWithLine(Boolean serialized, BigDecimal quantityReceived, BigDecimal exchangeRate, String serialNumber) {
        return new GoodsReceipt(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "GR-202604-00001",
                LocalDate.of(2026, 4, 26),
                7L,
                11L,
                3L,
                1L,
                exchangeRate,
                GoodsReceiptStatus.DRAFT,
                "draft",
                List.of(GoodsReceiptLine.prefill(
                        101L,
                        201L,
                        3L,
                        serialized,
                        quantityReceived,
                        1L,
                        99L,
                        new BigDecimal("150.0000"),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        serialNumber
                ))
        );
    }

    private PurchaseOrder sentPoWithOutstanding(String outstandingQuantity) {
        return sentPoWithOutstanding(outstandingQuantity, BigDecimal.ONE);
    }

    private PurchaseOrder sentPoWithOutstanding(String outstandingQuantity, BigDecimal exchangeRate) {
        BigDecimal quantity = new BigDecimal("12.0000");
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
                new BigDecimal("1800.0000"),
                BigDecimal.ZERO,
                new BigDecimal("1800.0000"),
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
                exchangeRate,
                new BigDecimal("1800.0000"),
                BigDecimal.ZERO,
                new BigDecimal("1800.0000"),
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
