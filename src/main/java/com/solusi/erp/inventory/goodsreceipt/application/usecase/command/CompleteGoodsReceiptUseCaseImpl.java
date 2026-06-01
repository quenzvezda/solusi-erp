package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.accounting.journal.application.usecase.command.JournalPostingCommand;
import com.solusi.erp.accounting.journal.application.usecase.command.PostJournalForEventUseCase;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
import com.solusi.erp.inventory.shared.util.SerialNumberGenerator;
import com.solusi.erp.inventory.stock.application.dto.StockMovementPayload;
import com.solusi.erp.inventory.stock.domain.model.MovementType;
import com.solusi.erp.inventory.stock.domain.model.ReferenceType;
import com.solusi.erp.inventory.stock.domain.port.StockService;
import com.solusi.erp.inventory.uomconversion.domain.port.UomConversionService;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private final UomConversionService uomConversionService;
    private final PostJournalForEventUseCase postJournalForEventUseCase;

    public CompleteGoodsReceiptUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository,
                                           PurchaseOrderRepository purchaseOrderRepository,
                                           EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase,
                                           StockService stockService,
                                           UomConversionService uomConversionService,
                                           PostJournalForEventUseCase postJournalForEventUseCase) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.ensureOpenPeriodForDateUseCase = ensureOpenPeriodForDateUseCase;
        this.stockService = stockService;
        this.uomConversionService = uomConversionService;
        this.postJournalForEventUseCase = postJournalForEventUseCase;
    }

    @Override
    public void execute(Long id) {
        GoodsReceipt receipt = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gr.notfound"));
        ensureOpenPeriodForDateUseCase.execute(receipt.getReceiptDate());

        PurchaseOrder po = purchaseOrderRepository.findById(receipt.getPoId())
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        validateLatestOutstanding(po, receipt.getLines());

        // Snapshot values from reference document before completing
        List<GoodsReceiptLine> snapshottedLines = snapshotReferenceValues(receipt, po);
        List<GoodsReceiptLine> completionReadyLines = enrichSerializedLinesWithResolvedSerials(snapshottedLines);
        receipt.update(receipt.getReceiptDate(), receipt.getNote(), completionReadyLines);

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

        BigDecimal inventoryTotal = receipt.getLines().stream()
                .map(GoodsReceiptLine::getInventoryAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxTotal = receipt.getLines().stream()
                .map(GoodsReceiptLine::getTaxAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        postJournalForEventUseCase.execute(new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                receipt.getId(),
                receipt.getCode(),
                receipt.getReceiptDate(),
                "Auto journal for goods receipt " + receipt.getCode(),
                Map.of(
                        JournalVariable.GR_INVENTORY_AMT, inventoryTotal,
                        JournalVariable.GR_TAX_AMT, BigDecimal.ZERO,
                        JournalVariable.GR_GRAND_TOTAL, inventoryTotal
                )
        ));

        goodsReceiptRepository.save(receipt);
        purchaseOrderRepository.save(po);
    }

    private List<GoodsReceiptLine> snapshotReferenceValues(GoodsReceipt receipt, PurchaseOrder po) {
        Map<Long, PurchaseOrderLine> poLines = po.getLines().stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, l -> l));

        return receipt.getLines().stream().map(line -> {
            PurchaseOrderLine poLine = poLines.get(line.getReferenceLineId());
            if (poLine == null) return line;

            BigDecimal unitPrice = poLine.getUnitPrice();
            BigDecimal quantityReceived = line.getQuantityReceived();

            // Calculate base quantity
            BigDecimal baseQuantity = quantityReceived;
            if (line.getUomId() != null) {
                baseQuantity = uomConversionService.convertToBaseUom(
                        line.getProductId(),
                        line.getUomId(),
                        quantityReceived
                );
            }

            // Amounts snapshot
            BigDecimal inventoryAmount = quantityReceived.multiply(unitPrice).setScale(4, RoundingMode.HALF_UP);
            BigDecimal taxBaseAmount = inventoryAmount; // Currently assuming tax base is net amount
            BigDecimal taxAmount = inventoryAmount.multiply(poLine.getTaxRate()).setScale(4, RoundingMode.HALF_UP);
            BigDecimal grIrAmount = inventoryAmount.add(taxAmount).setScale(4, RoundingMode.HALF_UP);

            return GoodsReceiptLine.prefill(
                    line.getReferenceLineId(),
                    line.getProductId(),
                    line.getSourceFacilityId(),
                    line.getSerialized(),
                    quantityReceived,
                    line.getUomId(),
                    line.getContainerId(),
                    unitPrice,
                    baseQuantity,
                    inventoryAmount,
                    taxBaseAmount,
                    taxAmount,
                    grIrAmount,
                    line.getSerialNumber()
            );
        }).toList();
    }

    private List<GoodsReceiptLine> enrichSerializedLinesWithResolvedSerials(List<GoodsReceiptLine> lines) {
        return lines.stream().map(line -> {
            if (!Boolean.TRUE.equals(line.getSerialized()) || !line.hasReceiptQuantity()) {
                return line;
            }
            int totalUnits = resolveSerializedUnitCount(line);
            List<String> serialNumbers = resolveSerialNumbers(line, totalUnits);
            return GoodsReceiptLine.prefill(
                    line.getReferenceLineId(),
                    line.getProductId(),
                    line.getSourceFacilityId(),
                    line.getSerialized(),
                    line.getQuantityReceived(),
                    line.getUomId(),
                    line.getContainerId(),
                    line.getUnitPrice(),
                    line.getBaseQuantity(),
                    line.getInventoryAmount(),
                    line.getTaxBaseAmount(),
                    line.getTaxAmount(),
                    line.getGrIrAmount(),
                    String.join(",", serialNumbers)
            );
        }).toList();
    }

    private void processSerializedLine(GoodsReceipt receipt, PurchaseOrder po, GoodsReceiptLine line) {
        int totalUnits = resolveSerializedUnitCount(line);
        List<String> serialNumbers = resolveSerialNumbers(line, totalUnits);
        for (String serialNumber : serialNumbers) {
            stockService.adjust(buildSerializedPayload(receipt, po, line, serialNumber, totalUnits));
        }
    }

    private List<String> resolveSerialNumbers(GoodsReceiptLine line, int totalUnits) {
        List<String> providedSerials = parseProvidedSerialNumbers(line.getSerialNumber());
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
        BigDecimal serializedUnitCount = line.getQuantityReceived();
        if (line.getUomId() != null && serializedUnitCount != null) {
            serializedUnitCount = uomConversionService.convertToBaseUom(
                    line.getProductId(),
                    line.getUomId(),
                    serializedUnitCount
            );
        }
        if (serializedUnitCount == null || serializedUnitCount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.gr.serial.quantity.whole");
        }
        try {
            return serializedUnitCount.abs().intValueExact();
        } catch (ArithmeticException ex) {
            throw new DomainException("msg.error.gr.serial.quantity.whole");
        }
    }

    private StockMovementPayload buildSerializedPayload(GoodsReceipt receipt, PurchaseOrder po, GoodsReceiptLine line,
                                                        String serialNumber, int totalUnits) {
        BigDecimal quantityReceived = line.getQuantityReceived();
        if (quantityReceived == null || quantityReceived.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.gr.serial.quantity.whole");
        }

        BigDecimal unitPrice = line.getUnitPrice() != null ? line.getUnitPrice() : BigDecimal.ZERO;
        BigDecimal serializedUnitPrice = unitPrice.multiply(quantityReceived)
                .divide(BigDecimal.valueOf(totalUnits), 6, RoundingMode.HALF_UP);

        return StockMovementPayload.builder()
                .productId(line.getProductId())
                .containerId(line.getContainerId())
                .serialNumber(serialNumber)
                .quantity(BigDecimal.ONE)
                .uomId(null)
                .movementType(MovementType.RECEIPT)
                .referenceType(ReferenceType.GOODS_RECEIPT)
                .referenceId(receipt.getId())
                .referenceCode(receipt.getCode())
                .valuationReferenceType(ReferenceType.GOODS_RECEIPT)
                .valuationReferenceId(receipt.getId())
                .valuationReferenceLineId(line.getId())
                .currencyId(receipt.getCurrencyId())
                .exchangeRate(po.getExchangeRate())
                .netPrice(serializedUnitPrice)
                .transactionDate(receipt.getReceiptDate().atStartOfDay())
                .build();
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
                .valuationReferenceType(ReferenceType.GOODS_RECEIPT)
                .valuationReferenceId(receipt.getId())
                .valuationReferenceLineId(line.getId())
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
                .collect(Collectors.groupingBy(GoodsReceiptLine::getReferenceLineId,
                        Collectors.mapping(GoodsReceiptLine::getQuantityReceived,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
    }
}
