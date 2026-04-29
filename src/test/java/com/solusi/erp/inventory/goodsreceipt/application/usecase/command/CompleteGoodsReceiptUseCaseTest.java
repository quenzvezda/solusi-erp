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
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
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

import static org.assertj.core.api.Assertions.assertThat;
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
    @Mock
    private UomConversionService uomConversionService;

    private CompleteGoodsReceiptUseCase completeUseCase;

    @BeforeEach
    void setUp() {
        completeUseCase = new CompleteGoodsReceiptUseCaseImpl(
                goodsReceiptRepository,
                purchaseOrderRepository,
                ensureOpenPeriodForDateUseCase,
                stockService,
                uomConversionService
        );
    }

    @Test
    void complete_callsStockServiceAndUpdatesPurchaseOrder() {
        GoodsReceipt receipt = draftReceiptWithOneActiveLine();
        assertThat(receipt.getLines().getFirst().getReferenceLineId()).isEqualTo(101L);
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
        when(uomConversionService.convertToBaseUom(201L, 1L, new BigDecimal("1.5000")))
                .thenReturn(new BigDecimal("1.5000"));

        assertThatThrownBy(() -> completeUseCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.serial.quantity.whole");
    }

    @Test
    void complete_serializedLinePostsOneUnitPerSerialAndGeneratesMissingSerials() {
        GoodsReceipt receipt = receiptWithLine(true, new BigDecimal("3.0000"), BigDecimal.ONE, "SN-1,SN-2");
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));
        when(uomConversionService.convertToBaseUom(201L, 1L, new BigDecimal("3.0000")))
                .thenReturn(new BigDecimal("3.0000"));

        completeUseCase.execute(1L);

        verify(stockService, times(3)).adjust(argThat((StockMovementPayload payload) ->
                payload.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && payload.getSerialNumber() != null
                        && payload.getUomId() == null));
        verify(stockService).adjust(argThat(payload ->
                payload.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && "SN-1".equals(payload.getSerialNumber())));
        verify(stockService).adjust(argThat(payload ->
                payload.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && "SN-2".equals(payload.getSerialNumber())));
        verify(stockService).adjust(argThat(payload ->
                payload.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && payload.getSerialNumber().startsWith("SN-")
                        && !"SN-1".equals(payload.getSerialNumber())
                        && !"SN-2".equals(payload.getSerialNumber())));
    }

    @Test
    void complete_serializedNonBaseUomPostsOneBaseUnitPerSerial() {
        GoodsReceipt receipt = receiptWithLine(
                true,
                new BigDecimal("1.5000"),
                BigDecimal.ZERO,
                2L,
                BigDecimal.ONE,
                "SN-1,SN-2"
        );
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));
        when(uomConversionService.convertToBaseUom(201L, 2L, new BigDecimal("1.5000")))
                .thenReturn(new BigDecimal("6.0000"));

        completeUseCase.execute(1L);

        verify(stockService, times(6)).adjust(argThat((StockMovementPayload payload) ->
                payload.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && payload.getSerialNumber() != null
                        && payload.getUomId() == null
                        && payload.getNetPrice().compareTo(new BigDecimal("37.500000")) == 0));
        verify(stockService).adjust(argThat(payload ->
                payload.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && "SN-1".equals(payload.getSerialNumber())));
        verify(stockService).adjust(argThat(payload ->
                payload.getQuantity().compareTo(BigDecimal.ONE) == 0
                        && "SN-2".equals(payload.getSerialNumber())));
    }

    @Test
    void complete_whenSerializedNonBaseUomConvertsToFraction_throws() {
        GoodsReceipt receipt = receiptWithLine(
                true,
                new BigDecimal("1.5000"),
                BigDecimal.ZERO,
                2L,
                BigDecimal.ONE,
                "SN-1"
        );
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));
        when(uomConversionService.convertToBaseUom(201L, 2L, new BigDecimal("1.5000")))
                .thenReturn(new BigDecimal("1.5000"));

        assertThatThrownBy(() -> completeUseCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.serial.quantity.whole");
    }

    @Test
    void complete_whenProvidedSerialCountExceedsUnitCount_throws() {
        GoodsReceipt receipt = receiptWithLine(true, new BigDecimal("1.0000"), BigDecimal.ONE, "SN-1,SN-2");
        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(sentPoWithOutstanding("10.0000")));
        when(uomConversionService.convertToBaseUom(201L, 1L, new BigDecimal("1.0000")))
                .thenReturn(new BigDecimal("1.0000"));

        assertThatThrownBy(() -> completeUseCase.execute(1L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.gr.serial.quantity.whole");
    }

    @Test
    void complete_snapshotsPricesAndAmountsFromPurchaseOrder() {
        BigDecimal qtyReceived = new BigDecimal("5.0000");
        GoodsReceipt receipt = receiptWithLine(false, qtyReceived, BigDecimal.ONE, null);

        // PO Line with Price 100 and Tax 10% (0.1)
        PurchaseOrder po = sentPoWithPriceAndTax("100.0000", "0.1000");

        when(goodsReceiptRepository.findById(1L)).thenReturn(Optional.of(receipt));
        when(purchaseOrderRepository.findById(receipt.getPoId())).thenReturn(Optional.of(po));
        when(uomConversionService.convertToBaseUom(201L, 1L, qtyReceived)).thenReturn(qtyReceived);

        completeUseCase.execute(1L);

        verify(goodsReceiptRepository).save(argThat(saved -> {
            GoodsReceiptLine line = saved.getLines().getFirst();
            // inventoryAmount = 5 * 100 = 500.0000
            // taxAmount = 500 * 0.1 = 50.0000
            // grIrAmount = 500 + 50 = 550.0000
            return line.getUnitPrice().compareTo(new BigDecimal("100.0000")) == 0 &&
                    line.getInventoryAmount().compareTo(new BigDecimal("500.0000")) == 0 &&
                    line.getTaxAmount().compareTo(new BigDecimal("50.0000")) == 0 &&
                    line.getGrIrAmount().compareTo(new BigDecimal("550.0000")) == 0;
        }));
    }

    private GoodsReceipt draftReceiptWithOneActiveLine() {
        return receiptWithLine(false, new BigDecimal("3.0000"), BigDecimal.ONE, null);
    }

    private GoodsReceipt receiptWithLine(Boolean serialized, BigDecimal quantityReceived, BigDecimal exchangeRate, String serialNumber) {
        return receiptWithLine(serialized, quantityReceived, BigDecimal.ZERO, 1L, exchangeRate, serialNumber);
    }

    private GoodsReceipt receiptWithLine(Boolean serialized, BigDecimal quantityReceived, BigDecimal baseQuantity,
                                         Long uomId, BigDecimal exchangeRate, String serialNumber) {
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
                        uomId,
                        99L,
                        new BigDecimal("150.0000"),
                        baseQuantity,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        serialNumber
                ))
        );
    }

    private PurchaseOrder sentPoWithPriceAndTax(String unitPrice, String taxRate) {
        BigDecimal quantity = new BigDecimal("10.0000");
        PurchaseOrderLine line = PurchaseOrderLine.rehydrate(
                new AuditMetadata(101L, 1L, null, null, null, null),
                7L,
                201L,
                quantity,
                BigDecimal.ZERO,
                1L,
                new BigDecimal(unitPrice),
                new BigDecimal(taxRate),
                new BigDecimal(unitPrice).multiply(quantity),
                new BigDecimal(unitPrice).multiply(quantity).multiply(new BigDecimal(taxRate)),
                new BigDecimal(unitPrice).multiply(quantity).multiply(BigDecimal.ONE.add(new BigDecimal(taxRate))),
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
                new BigDecimal("1000.0000"),
                BigDecimal.ZERO,
                new BigDecimal("1000.0000"),
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
