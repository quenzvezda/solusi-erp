package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
import com.solusi.erp.inventory.shared.util.SerialNumberGenerator;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompleteGoodsReceiptUseCaseImpl implements CompleteGoodsReceiptUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private final StockService stockService;

    public CompleteGoodsReceiptUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository,
                                           PurchaseOrderRepository purchaseOrderRepository,
                                           EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                           StockService stockService) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
        this.stockService = stockService;
    }

    @Override
    public void execute(Long id) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gr.notfound"));
        ensureOpenPeriodForDateUseCase.execute(receipt.getReceiptDate());

        PurchaseOrder po = purchaseOrderRepository.findById(receipt.getPoId())
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        validateLatestOutstanding(po, receipt.getLines());

        receipt.complete();
        for (GoodsReceiptLine line : receipt.getLines()) {
            if (!line.hasReceiptQuantity()) {
                continue;
            }
            if (Boolean.TRUE.equals(line.getSerialized())) {
                processSerializedLine(receipt, po, line);
            } else {
                stockService.adjust(buildBasePayload(receipt, po, line, line.getQuantityReceived(), line.getSerialNumber()));
            }
        }

        po.recordReceipt(sumByPoLine(receipt.getLines()));
        goodsReceiptRepository.save(receipt);
        purchaseOrderRepository.save(po);
    }

    private void processSerializedLine(GoodsReceipt receipt, PurchaseOrder po, GoodsReceiptLine line) {
        List<String> serialNumbers = resolveSerialNumbers(line);
        for (String serialNumber : serialNumbers) {
            stockService.adjust(buildBasePayload(receipt, po, line, BigDecimal.ONE, serialNumber));
        }
    }

    private List<String> resolveSerialNumbers(GoodsReceiptLine line) {
        List<String> providedSerials = parseProvidedSerialNumbers(line.getSerialNumber());
        int totalUnits = resolveSerializedUnitCount(line);
        if (providedSerials.size() > totalUnits) {
            throw new DomainException("msg.error.gr.serial.quantity.whole");
        }
        List<String> serials = new ArrayList<>(totalUnits);

        serials.addAll(providedSerials);
        while (serials.size() < totalUnits) {
            serials.add(SerialNumberGenerator.generate());
        }
        return serials;
    }

    private List<String> parseProvidedSerialNumbers(String serialNumber) {
        if (!StringUtils.hasText(serialNumber)) {
            return List.of();
        }
        return Arrays.stream(serialNumber.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private int resolveSerializedUnitCount(GoodsReceiptLine line) {
        if (line.getQuantityReceived() == null) {
            throw new DomainException("msg.error.gr.serial.quantity.whole");
        }
        try {
            return line.getQuantityReceived().abs().intValueExact();
        } catch (ArithmeticException ex) {
            throw new DomainException("msg.error.gr.serial.quantity.whole");
        }
    }

    private StockMovementPayload buildBasePayload(GoodsReceipt receipt, PurchaseOrder po, GoodsReceiptLine line,
                                                  BigDecimal quantity, String serialNumber) {
        return StockMovementPayload.builder()
                .productId(line.getProductId())
                .containerId(line.getContainerId())
                .serialNumber(serialNumber)
                .quantity(quantity)
                .uomId(line.getUomId())
                .movementType(MovementType.RECEIPT)
                .referenceType(ReferenceType.GOODS_RECEIPT)
                .referenceId(receipt.getId())
                .referenceCode(receipt.getCode())
                .currencyId(receipt.getCurrencyId())
                .exchangeRate(po.getExchangeRate())
                .netPrice(line.getUnitPrice())
                .transactionDate(receipt.getReceiptDate().atStartOfDay())
                .build();
    }

    private void validateLatestOutstanding(PurchaseOrder po, List<GoodsReceiptLine> lines) {
        Map<Long, BigDecimal> outstandingByPoLine = po.getLines().stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, PurchaseOrderLine::getOutstandingQuantity));
        Map<Long, BigDecimal> requestedByPoLine = sumByPoLine(lines);

        for (Map.Entry<Long, BigDecimal> entry : requestedByPoLine.entrySet()) {
            BigDecimal outstanding = outstandingByPoLine.get(entry.getKey());
            if (outstanding == null || entry.getValue().compareTo(outstanding) > 0) {
                throw new DomainException("msg.error.gr.stale.po.changed");
            }
        }
    }

    private Map<Long, BigDecimal> sumByPoLine(List<GoodsReceiptLine> lines) {
        return lines.stream()
                .filter(GoodsReceiptLine::hasReceiptQuantity)
                .collect(Collectors.groupingBy(GoodsReceiptLine::getPoLineId,
                        Collectors.mapping(GoodsReceiptLine::getQuantityReceived,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }
}
