package com.solusi.erp.inventory.goodsreceipt.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.core.infrastructure.sequence.SequenceGeneratorService;
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

public class CreateGoodsReceiptUseCaseImpl implements CreateGoodsReceiptUseCase {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    public CreateGoodsReceiptUseCaseImpl(GoodsReceiptRepository goodsReceiptRepository,
                                         PurchaseOrderRepository purchaseOrderRepository,
                                         SequenceGeneratorService sequenceGeneratorService) {
        this.goodsReceiptRepository = goodsReceiptRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.sequenceGeneratorService = sequenceGeneratorService;
    }

    @Override
    public GoodsReceipt execute(LocalDate receiptDate, Long poId, String note, List<GoodsReceiptLineCommand> lines) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
                .orElseThrow(() -> new DomainException("msg.error.po.notfound"));
        validateReceivable(po);

        List<GoodsReceiptLineCommand> normalizedCommands = normalizeCommands(lines);
        validateLatestOutstanding(po, normalizedCommands);

        GoodsReceipt goodsReceipt = GoodsReceipt.createNew(
                sequenceGeneratorService.generate("GOODS_RECEIPT"),
                receiptDate,
                po.getId(),
                po.getSupplierId(),
                po.getFacilityId(),
                po.getCurrencyId(),
                po.getExchangeRate(),
                buildLines(po, normalizedCommands)
        );
        goodsReceipt.update(receiptDate, note, goodsReceipt.getLines());
        return goodsReceiptRepository.save(goodsReceipt);
    }

    private List<GoodsReceiptLineCommand> normalizeCommands(List<GoodsReceiptLineCommand> lines) {
        return lines == null ? List.of() : lines;
    }

    private List<GoodsReceiptLine> buildLines(PurchaseOrder po, List<GoodsReceiptLineCommand> commands) {
        Map<Long, PurchaseOrderLine> poLines = po.getLines().stream()
                .collect(Collectors.toMap(PurchaseOrderLine::getId, Function.identity()));

        return commands.stream()
                .filter(this::hasPositiveQuantity)
                .map(command -> toLine(po, poLines.get(command.poLineId()), command))
                .toList();
    }

    private GoodsReceiptLine toLine(PurchaseOrder po, PurchaseOrderLine poLine, GoodsReceiptLineCommand command) {
        if (poLine == null) {
            throw new DomainException("msg.error.gr.stale.po.changed");
        }
        return GoodsReceiptLine.prefill(
                command.poLineId(),
                command.productId() != null ? command.productId() : poLine.getProductId(),
                po.getFacilityId(),
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
                .collect(Collectors.groupingBy(GoodsReceiptLineCommand::poLineId,
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
