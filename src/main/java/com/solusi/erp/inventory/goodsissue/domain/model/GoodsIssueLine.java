package com.solusi.erp.inventory.goodsissue.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.math.BigDecimal;

public final class GoodsIssueLine {

    private final AuditMetadata metadata;
    private final Long referenceLineId;
    private final Long productId;
    private final Boolean serialized;
    private final BigDecimal quantityIssued;
    private final Long uomId;
    private final BigDecimal baseQuantity;
    private final Long facilityId;
    private final Long gridId;
    private final Long containerId;
    private final String serialNumber;
    private final BigDecimal unitCost;
    private final BigDecimal inventoryAmount;
    private final BigDecimal taxBaseAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal clearingAmount;
    private final String valuationRefType;
    private final Long valuationRefId;
    private final Long valuationRefLineId;

    private GoodsIssueLine(AuditMetadata metadata, Long referenceLineId, Long productId, Boolean serialized,
                           BigDecimal quantityIssued, Long uomId, BigDecimal baseQuantity,
                           Long facilityId, Long gridId, Long containerId, String serialNumber,
                           BigDecimal unitCost, BigDecimal inventoryAmount, BigDecimal taxBaseAmount,
                           BigDecimal taxAmount, BigDecimal clearingAmount, String valuationRefType,
                           Long valuationRefId, Long valuationRefLineId) {
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.referenceLineId = referenceLineId;
        this.productId = productId;
        this.serialized = serialized;
        this.quantityIssued = quantityIssued;
        this.uomId = uomId;
        this.baseQuantity = baseQuantity;
        this.facilityId = facilityId;
        this.gridId = gridId;
        this.containerId = containerId;
        this.serialNumber = serialNumber;
        this.unitCost = unitCost;
        this.inventoryAmount = inventoryAmount;
        this.taxBaseAmount = taxBaseAmount;
        this.taxAmount = taxAmount;
        this.clearingAmount = clearingAmount;
        this.valuationRefType = valuationRefType;
        this.valuationRefId = valuationRefId;
        this.valuationRefLineId = valuationRefLineId;
    }

    public static GoodsIssueLine prefill(Long referenceLineId, Long productId, Boolean serialized,
                                         BigDecimal quantityIssued, Long uomId, BigDecimal baseQuantity,
                                         Long facilityId, Long gridId, Long containerId, String serialNumber,
                                         BigDecimal unitCost, BigDecimal inventoryAmount,
                                         BigDecimal taxBaseAmount, BigDecimal taxAmount,
                                         BigDecimal clearingAmount, String valuationRefType,
                                         Long valuationRefId, Long valuationRefLineId) {
        return new GoodsIssueLine(
                AuditMetadata.empty(), referenceLineId, productId, serialized, quantityIssued, uomId,
                baseQuantity, facilityId, gridId, containerId, serialNumber, unitCost, inventoryAmount,
                taxBaseAmount, taxAmount, clearingAmount, valuationRefType, valuationRefId, valuationRefLineId
        );
    }

    public static GoodsIssueLine reconstitute(AuditMetadata metadata, Long referenceLineId, Long productId,
                                              Boolean serialized, BigDecimal quantityIssued, Long uomId,
                                              BigDecimal baseQuantity, Long facilityId, Long gridId,
                                              Long containerId, String serialNumber, BigDecimal unitCost,
                                              BigDecimal inventoryAmount, BigDecimal taxBaseAmount,
                                              BigDecimal taxAmount, BigDecimal clearingAmount,
                                              String valuationRefType, Long valuationRefId,
                                              Long valuationRefLineId) {
        return new GoodsIssueLine(
                metadata, referenceLineId, productId, serialized, quantityIssued, uomId, baseQuantity,
                facilityId, gridId, containerId, serialNumber, unitCost, inventoryAmount, taxBaseAmount,
                taxAmount, clearingAmount, valuationRefType, valuationRefId, valuationRefLineId
        );
    }

    public boolean hasIssueQuantity() {
        return quantityIssued != null && quantityIssued.compareTo(BigDecimal.ZERO) > 0;
    }

    public Long getId() {
        return metadata.id();
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    public Long getReferenceLineId() {
        return referenceLineId;
    }

    public Long getProductId() {
        return productId;
    }

    public Boolean getSerialized() {
        return serialized;
    }

    public BigDecimal getQuantityIssued() {
        return quantityIssued;
    }

    public Long getUomId() {
        return uomId;
    }

    public BigDecimal getBaseQuantity() {
        return baseQuantity;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public Long getGridId() {
        return gridId;
    }

    public Long getContainerId() {
        return containerId;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public BigDecimal getUnitCost() {
        return unitCost;
    }

    public BigDecimal getInventoryAmount() {
        return inventoryAmount;
    }

    public BigDecimal getTaxBaseAmount() {
        return taxBaseAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getClearingAmount() {
        return clearingAmount;
    }

    public String getValuationRefType() {
        return valuationRefType;
    }

    public Long getValuationRefId() {
        return valuationRefId;
    }

    public Long getValuationRefLineId() {
        return valuationRefLineId;
    }
}
