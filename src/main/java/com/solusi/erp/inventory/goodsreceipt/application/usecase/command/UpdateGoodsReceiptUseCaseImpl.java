package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceipt;
import com.solusi.erp.inventory.goodsreceipt.domain.model.GoodsReceiptLine;
import com.solusi.erp.inventory.goodsreceipt.domain.repository.GoodsReceiptRepository;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrder;
import com.solusi.erp.purchasing.purchaseorder.domain.model.PurchaseOrderLine;
import com.solusi.erp.purchasing.purchaseorder.domain.repository.PurchaseOrderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UpdateGoodsReceiptUseCaseImpl implements UpdateGoodsReceiptUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public UpdateGoodsReceiptUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository,
                                         PurchaseOrderRepository purchaseOrderRepository) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Override
    public GoodsReceipt execute(Long id, LocalDate receiptDate, String note, List<GoodsReceiptLineCommand> lines) {
        GoodsReceipt goodsReceipt = goodsReceiptRepository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.gr.notfound"));
        PurchaseOrder po = purchaseOrderRepository.findById(goodsReceipt.getPoId())
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        validateReceivable(po);

        List<GoodsReceiptLineCommand> normalizedCommands = normalizeCommands(lines);
        validateLatestOutstanding(po, normalizedCommands);

        goodsReceipt.update(receiptDate, note, buildLines(goodsReceipt, po, normalizedCommands));
        return goodsReceiptRepository.save(goodsReceipt);
    }

    private List<GoodsReceiptLineCommand> normalizeCommands(List<GoodsReceiptLineCommand> lines) {
        return lines == null ? List.of() : lines;
    }

    private List<GoodsReceiptLine> buildLines(GoodsReceipt goodsReceipt, PurchaseOrder po, List<GoodsReceiptLineCommand> commands) {
        Map<Long, PurchaseOrderLine> poLines = po.getLines().stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, Function.identity()));

        return commands.stream()
                .filter(this::hasPositiveQuantity)
                .map(command -> toLine(goodsReceipt, poLines.get(command.referenceLineId()), command))
                .toList();
    }

    private GoodsReceiptLine toLine(GoodsReceipt goodsReceipt, PurchaseOrderLine poLine, GoodsReceiptLineCommand command) {
        if (poLine == null) {
            throw new DomainException("msg.error.gr.stale.po.changed");
        }
        return GoodsReceiptLine.prefill(
                command.referenceLineId(),
                command.productId() != null ? command.productId() : poLine.getProductId(),
                goodsReceipt.getFacilityId(),
                Boolean.TRUE.equals(command.serialized()),
                command.quantityReceived(),
                command.uomId() != null ? command.uomId() : poLine.getUomId(),
                command.containerId(),
                poLine.getUnitPrice(),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                command.serialNumber()
        );
    }

    private void validateReceivable(PurchaseOrder po) {
        if (!po.getStatus().canReceive()) {
            throw new DomainException("msg.error.gr.po.invalid.status");
        }
    }

    private void validateLatestOutstanding(PurchaseOrder po, List<GoodsReceiptLineCommand> commands) {
        Map<Long, BigDecimal> outstandingByPoLine = po.getLines().stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, PurchaseOrderLine::getOutstandingQuantity));
        Map<Long, BigDecimal> requestedByPoLine = commands.stream()
                .filter(this::hasPositiveQuantity)
                .collect(Collectors.groupingBy(GoodsReceiptLineCommand::referenceLineId,
                        Collectors.mapping(GoodsReceiptLineCommand::quantityReceived,
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));
        for (Map.Entry<Long, BigDecimal> entry : requestedByPoLine.entrySet()) {
            BigDecimal outstanding = outstandingByPoLine.getOrDefault(entry.getKey(), BigDecimal.ZERO);
            if (entry.getValue().compareTo(outstanding) > 0) {
                throw new DomainException("msg.error.gr.stale.po.changed");
            }
        }
    }

    private boolean hasPositiveQuantity(GoodsReceiptLineCommand command) {
        return command.quantityReceived() != null && command.quantityReceived().compareTo(BigDecimal.ZERO) > 0;
    }
}
