package com.solusi.erp.purchasing.purchasereturn.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.EligibleGoodsReceiptRow;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableGrLineSlice;
import com.solusi.erp.purchasing.purchasereturn.application.usecase.query.ReturnableSerialRow;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnLine;
import com.solusi.erp.purchasing.purchasereturn.domain.port.PurchaseReturnSourceQueryPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class PurchaseReturnDraftLineFactory {

    private final PurchaseReturnSourceQueryPort sourceQueryPort;

    PurchaseReturnDraftLineFactory(PurchaseReturnSourceQueryPort sourceQueryPort) {
        this.sourceQueryPort = sourceQueryPort;
    }

    EligibleGoodsReceiptRow requireSource(Long goodsReceiptId) {
        return sourceQueryPort.findEligibleGoodsReceiptById(goodsReceiptId)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.source.ineligible"));
    }

    List<PurchaseReturnLine> buildLines(Long goodsReceiptId, List<PurchaseReturnLineCommand> commands) {
        if (commands == null) {
            return List.of();
        }
        return commands.stream()
                .filter(this::hasPositiveQuantity)
                .map(command -> command.serialized()
                        ? buildSerializedLine(goodsReceiptId, command)
                        : buildNonSerializedLine(goodsReceiptId, command))
                .toList();
    }

    private PurchaseReturnLine buildNonSerializedLine(Long goodsReceiptId, PurchaseReturnLineCommand command) {
        ReturnableGrLineSlice slice = sourceQueryPort
                .findReturnableGrLineSlices(goodsReceiptId, null, List.of())
                .stream()
                .filter(candidate -> candidate.goodsReceiptLineId().equals(command.goodsReceiptLineId()))
                .filter(candidate -> candidate.containerId().equals(command.containerId()))
                .findFirst()
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.line.stale"));
        if (command.quantity().compareTo(slice.outstandingQuantity()) > 0) {
            throw new DomainException("msg.error.purchase-return.line.quantity-exceeds-returnable");
        }
        BigDecimal ratio = ratio(command.quantity(), slice.outstandingQuantity());
        return PurchaseReturnLine.create(
                slice.goodsReceiptLineId(), slice.productId(), false, command.quantity(), slice.uomId(),
                command.quantity(), slice.facilityId(), slice.gridId(), slice.containerId(), null,
                command.reason(), command.note(), slice.valuationReferenceType(), slice.valuationReferenceId(),
                slice.valuationReferenceLineId(), slice.unitCost(), scale(slice.inventoryAmount(), ratio),
                scale(slice.taxReversalAmount(), ratio), scale(slice.clearingAmount(), ratio)
        );
    }

    private PurchaseReturnLine buildSerializedLine(Long goodsReceiptId, PurchaseReturnLineCommand command) {
        List<String> selectedSerials = parseSerials(command.serialNumbers());
        if (selectedSerials.isEmpty() || new HashSet<>(selectedSerials).size() != selectedSerials.size()) {
            throw new DomainException("msg.error.purchase-return.line.serial-selection-invalid");
        }
        List<ReturnableSerialRow> eligible = sourceQueryPort.findReturnableSerials(
                goodsReceiptId, command.goodsReceiptLineId(), null, List.of());
        List<ReturnableSerialRow> selected = selectedSerials.stream()
                .map(serial -> eligible.stream()
                        .filter(candidate -> candidate.serialNumber().equals(serial))
                        .filter(candidate -> candidate.containerId().equals(command.containerId()))
                        .findFirst()
                        .orElseThrow(() -> new DomainException("msg.error.purchase-return.line.serial-stale")))
                .toList();
        ReturnableSerialRow first = selected.getFirst();
        BigDecimal quantity = BigDecimal.valueOf(selected.size());
        return PurchaseReturnLine.create(
                first.goodsReceiptLineId(), first.productId(), true, quantity, first.uomId(), quantity,
                first.facilityId(), first.gridId(), first.containerId(), String.join(",", selectedSerials),
                command.reason(), command.note(), first.valuationReferenceType(), first.valuationReferenceId(),
                first.valuationReferenceLineId(), first.unitCost(), sumInventory(selected),
                sumTax(selected), sumClearing(selected)
        );
    }

    private List<String> parseSerials(String serialNumbers) {
        if (serialNumbers == null || serialNumbers.isBlank()) {
            return List.of();
        }
        return Arrays.stream(serialNumbers.split(","))
                .map(String::trim)
                .filter(serial -> !serial.isEmpty())
                .toList();
    }

    private BigDecimal sumInventory(List<ReturnableSerialRow> rows) {
        return rows.stream().map(ReturnableSerialRow::inventoryAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumTax(List<ReturnableSerialRow> rows) {
        return rows.stream().map(ReturnableSerialRow::taxReversalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal sumClearing(List<ReturnableSerialRow> rows) {
        return rows.stream().map(ReturnableSerialRow::clearingAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean hasPositiveQuantity(PurchaseReturnLineCommand command) {
        return command != null && command.quantity() != null
                && command.quantity().compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal ratio(BigDecimal selected, BigDecimal outstanding) {
        return selected.divide(outstanding, 12, RoundingMode.HALF_UP);
    }

    private BigDecimal scale(BigDecimal amount, BigDecimal ratio) {
        return amount == null ? BigDecimal.ZERO : amount.multiply(ratio);
    }
}
