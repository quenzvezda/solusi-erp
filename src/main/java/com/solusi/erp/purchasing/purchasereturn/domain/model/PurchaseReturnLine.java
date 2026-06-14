package com.solusi.erp.purchasing.purchasereturn.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.util.Arrays;

public final class PurchaseReturnLine {

    private final AuditMetadata metadata;
    private final Long goodsReceiptLineId;
    private final Long productId;
    private final boolean serialized;
    private final BigDecimal quantity;
    private final Long uomId;
    private final BigDecimal baseQuantity;
    private final Long facilityId;
    private final Long gridId;
    private final Long containerId;
    private final String serialNumbers;
    private final PurchaseReturnReason reason;
    private final String note;
    private final String valuationReferenceType;
    private final Long valuationReferenceId;
    private final Long valuationReferenceLineId;
    private final BigDecimal unitCost;
    private final BigDecimal inventoryAmount;
    private final BigDecimal taxReversalAmount;
    private final BigDecimal clearingAmount;

    private PurchaseReturnLine(AuditMetadata metadata,
                               Long goodsReceiptLineId,
                               Long productId,
                               boolean serialized,
                               BigDecimal quantity,
                               Long uomId,
                               BigDecimal baseQuantity,
                               Long facilityId,
                               Long gridId,
                               Long containerId,
                               String serialNumbers,
                               PurchaseReturnReason reason,
                               String note,
                               String valuationReferenceType,
                               Long valuationReferenceId,
                               Long valuationReferenceLineId,
                               BigDecimal unitCost,
                               BigDecimal inventoryAmount,
                               BigDecimal taxReversalAmount,
                               BigDecimal clearingAmount) {
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.goodsReceiptLineId = goodsReceiptLineId;
        this.productId = productId;
        this.serialized = serialized;
        this.quantity = quantity;
        this.uomId = uomId;
        this.baseQuantity = baseQuantity;
        this.facilityId = facilityId;
        this.gridId = gridId;
        this.containerId = containerId;
        this.serialNumbers = serialNumbers;
        this.reason = reason;
        this.note = note;
        this.valuationReferenceType = valuationReferenceType;
        this.valuationReferenceId = valuationReferenceId;
        this.valuationReferenceLineId = valuationReferenceLineId;
        this.unitCost = unitCost;
        this.inventoryAmount = inventoryAmount;
        this.taxReversalAmount = defaultZero(taxReversalAmount);
        this.clearingAmount = defaultZero(clearingAmount);
    }

    public static PurchaseReturnLine create(Long goodsReceiptLineId,
                                            Long productId,
                                            boolean serialized,
                                            BigDecimal quantity,
                                            Long uomId,
                                            BigDecimal baseQuantity,
                                            Long facilityId,
                                            Long gridId,
                                            Long containerId,
                                            String serialNumbers,
                                            PurchaseReturnReason reason,
                                            String note,
                                            String valuationReferenceType,
                                            Long valuationReferenceId,
                                            Long valuationReferenceLineId,
                                            BigDecimal unitCost,
                                            BigDecimal inventoryAmount,
                                            BigDecimal taxReversalAmount,
                                            BigDecimal clearingAmount) {
        validate(quantity, baseQuantity, serialized, serialNumbers, reason, note,
                valuationReferenceType, valuationReferenceId, valuationReferenceLineId);
        return new PurchaseReturnLine(
                AuditMetadata.empty(), goodsReceiptLineId, productId, serialized, quantity, uomId,
                baseQuantity, facilityId, gridId, containerId, serialNumbers, reason, note,
                valuationReferenceType, valuationReferenceId, valuationReferenceLineId, unitCost,
                inventoryAmount, taxReversalAmount, clearingAmount
        );
    }

    public static PurchaseReturnLine reconstitute(AuditMetadata metadata,
                                                  Long goodsReceiptLineId,
                                                  Long productId,
                                                  boolean serialized,
                                                  BigDecimal quantity,
                                                  Long uomId,
                                                  BigDecimal baseQuantity,
                                                  Long facilityId,
                                                  Long gridId,
                                                  Long containerId,
                                                  String serialNumbers,
                                                  PurchaseReturnReason reason,
                                                  String note,
                                                  String valuationReferenceType,
                                                  Long valuationReferenceId,
                                                  Long valuationReferenceLineId,
                                                  BigDecimal unitCost,
                                                  BigDecimal inventoryAmount,
                                                  BigDecimal taxReversalAmount,
                                                  BigDecimal clearingAmount) {
        return new PurchaseReturnLine(
                metadata, goodsReceiptLineId, productId, serialized, quantity, uomId, baseQuantity,
                facilityId, gridId, containerId, serialNumbers, reason, note, valuationReferenceType,
                valuationReferenceId, valuationReferenceLineId, unitCost, inventoryAmount,
                taxReversalAmount, clearingAmount
        );
    }

    private static void validate(BigDecimal quantity,
                                 BigDecimal baseQuantity,
                                 boolean serialized,
                                 String serialNumbers,
                                 PurchaseReturnReason reason,
                                 String note,
                                 String valuationReferenceType,
                                 Long valuationReferenceId,
                                 Long valuationReferenceLineId) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0
                || baseQuantity == null || baseQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.purchase-return.line.quantity-positive");
        }
        if (reason == null) {
            throw new DomainException("msg.error.purchase-return.line.reason-required");
        }
        if (reason == PurchaseReturnReason.OTHER && isBlank(note)) {
            throw new DomainException("msg.error.purchase-return.line.other-note-required");
        }
        if (isBlank(valuationReferenceType) || valuationReferenceId == null || valuationReferenceLineId == null) {
            throw new DomainException("msg.error.purchase-return.line.valuation-reference-required");
        }
        if (serialized) {
            if (baseQuantity.stripTrailingZeros().scale() > 0) {
                throw new DomainException("msg.error.purchase-return.line.serial-whole-quantity");
            }
            if (serialCount(serialNumbers) != baseQuantity.intValueExact()) {
                throw new DomainException("msg.error.purchase-return.line.serial-count-mismatch");
            }
        }
    }

    private static int serialCount(String serialNumbers) {
        if (isBlank(serialNumbers)) {
            return 0;
        }
        return (int) Arrays.stream(serialNumbers.split(","))
                .map(String::trim)
                .filter(serial -> !serial.isEmpty())
                .count();
    }

    private static BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getGoodsReceiptLineId() { return goodsReceiptLineId; }
    public Long getProductId() { return productId; }
    public boolean isSerialized() { return serialized; }
    public BigDecimal getQuantity() { return quantity; }
    public Long getUomId() { return uomId; }
    public BigDecimal getBaseQuantity() { return baseQuantity; }
    public Long getFacilityId() { return facilityId; }
    public Long getGridId() { return gridId; }
    public Long getContainerId() { return containerId; }
    public String getSerialNumbers() { return serialNumbers; }
    public PurchaseReturnReason getReason() { return reason; }
    public String getNote() { return note; }
    public String getValuationReferenceType() { return valuationReferenceType; }
    public Long getValuationReferenceId() { return valuationReferenceId; }
    public Long getValuationReferenceLineId() { return valuationReferenceLineId; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getInventoryAmount() { return inventoryAmount; }
    public BigDecimal getTaxReversalAmount() { return taxReversalAmount; }
    public BigDecimal getClearingAmount() { return clearingAmount; }
}
