package com.solusi.erp.inventory.goodsissue.infrastructure.adapter;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssue;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueLine;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssuePartyType;
import com.solusi.erp.inventory.goodsissue.domain.model.GoodsIssueReferenceType;
import com.solusi.erp.inventory.goodsissue.domain.port.GoodsIssueSourceResolver;
import com.solusi.erp.inventory.goodsissue.domain.port.PurchaseReturnGoodsIssueSourcePort;

import java.math.BigDecimal;

public class PurchaseReturnGoodsIssueSourceResolver implements GoodsIssueSourceResolver {

    private final PurchaseReturnGoodsIssueSourcePort sourcePort;

    public PurchaseReturnGoodsIssueSourceResolver(PurchaseReturnGoodsIssueSourcePort sourcePort) {
        this.sourcePort = sourcePort;
    }

    @Override
    public GoodsIssueReferenceType getReferenceType() {
        return GoodsIssueReferenceType.PURCHASE_RETURN;
    }

    @Override
    public GoodsIssue resolve(Long referenceId) {
        PurchaseReturnGoodsIssueSourcePort.HeaderSnapshot header = sourcePort.findHeader(referenceId)
                .orElseThrow(() -> new DomainException("msg.error.purchase-return.not-found"));

        return GoodsIssue.createNew(
                null,
                header.returnDate(),
                GoodsIssueReferenceType.PURCHASE_RETURN,
                header.purchaseReturnId(),
                header.purchaseReturnCode(),
                header.supplierId(),
                GoodsIssuePartyType.SUPPLIER,
                header.facilityId(),
                header.currencyId(),
                header.exchangeRate(),
                sourcePort.findEligibleLines(referenceId).stream().map(this::toLine).toList()
        );
    }

    private GoodsIssueLine toLine(PurchaseReturnGoodsIssueSourcePort.LineSnapshot line) {
        return GoodsIssueLine.prefill(
                line.purchaseReturnLineId(),
                line.productId(),
                line.serialized(),
                line.quantity(),
                line.uomId(),
                line.baseQuantity(),
                line.facilityId(),
                line.gridId(),
                line.containerId(),
                line.serialNumber(),
                line.unitCost(),
                line.inventoryAmount(),
                BigDecimal.ZERO,
                line.taxReversalAmount(),
                line.clearingAmount(),
                line.valuationRefType(),
                line.valuationRefId(),
                line.valuationRefLineId()
        );
    }
}
