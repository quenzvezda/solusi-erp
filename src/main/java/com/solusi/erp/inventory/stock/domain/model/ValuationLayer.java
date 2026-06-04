package com.solusi.erp.inventory.stock.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.math.BigDecimal;

/**
 * Entity: ValuationLayer.
 * Represents a single FIFO cost layer for inventory valuation.
 * Pure Java Domain Model — no Spring, no Lombok, no JPA.
 */
public class ValuationLayer {

    private final AuditMetadata metadata;
    private final Long productId;
    private final Long containerId;
    private final String serialNumber;
    private final BigDecimal initialQuantity;
    private BigDecimal remainingQuantity;
    private final CostAmount unitCost;
    private final ReferenceType referenceType;
    private final Long referenceId;
    private final Long referenceLineId;
    private final Long reversalOfMovementId;

    public ValuationLayer(AuditMetadata metadata, Long productId, Long containerId, String serialNumber,
                          BigDecimal initialQuantity, BigDecimal remainingQuantity, CostAmount unitCost) {
        this(metadata, productId, containerId, serialNumber, initialQuantity, remainingQuantity, unitCost,
                null, null, null, null);
    }

    public ValuationLayer(AuditMetadata metadata, Long productId, Long containerId, String serialNumber,
                          BigDecimal initialQuantity, BigDecimal remainingQuantity, CostAmount unitCost,
                          ReferenceType referenceType, Long referenceId, Long referenceLineId) {
        this(metadata, productId, containerId, serialNumber, initialQuantity, remainingQuantity, unitCost,
                referenceType, referenceId, referenceLineId, null);
    }

    public ValuationLayer(AuditMetadata metadata, Long productId, Long containerId, String serialNumber,
                          BigDecimal initialQuantity, BigDecimal remainingQuantity, CostAmount unitCost,
                          ReferenceType referenceType, Long referenceId, Long referenceLineId,
                          Long reversalOfMovementId) {
        this.metadata = metadata;
        this.productId = productId;
        this.containerId = containerId;
        this.serialNumber = serialNumber;
        this.initialQuantity = initialQuantity;
        this.remainingQuantity = remainingQuantity;
        this.unitCost = unitCost;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.referenceLineId = referenceLineId;
        this.reversalOfMovementId = reversalOfMovementId;
    }

    public static ValuationLayer createNew(Long productId, Long containerId, String serialNumber,
                                           BigDecimal quantity, CostAmount unitCost) {
        return createNew(productId, containerId, serialNumber, quantity, unitCost, null, null, null);
    }

    public static ValuationLayer createNew(Long productId, Long containerId, String serialNumber,
                                           BigDecimal quantity, CostAmount unitCost,
                                           ReferenceType referenceType, Long referenceId, Long referenceLineId) {
        return createNew(productId, containerId, serialNumber, quantity, unitCost,
                referenceType, referenceId, referenceLineId, null);
    }

    public static ValuationLayer createNew(Long productId, Long containerId, String serialNumber,
                                           BigDecimal quantity, CostAmount unitCost,
                                           ReferenceType referenceType, Long referenceId, Long referenceLineId,
                                           Long reversalOfMovementId) {
        return new ValuationLayer(AuditMetadata.empty(), productId, containerId, serialNumber,
                quantity, quantity, unitCost, referenceType, referenceId, referenceLineId, reversalOfMovementId);
    }

    /**
     * Consume up to the given quantity from this layer.
     * Returns the total local cost consumed (qty consumed × unit local cost).
     *
     * @param qty the quantity to consume
     * @return the local cost for the consumed portion
     */
    public BigDecimal consume(BigDecimal qty) {
        BigDecimal canConsume = this.remainingQuantity.min(qty);
        BigDecimal localCost = canConsume.multiply(this.unitCost.localAmount());
        this.remainingQuantity = this.remainingQuantity.subtract(canConsume);
        return localCost;
    }

    /**
     * How much of the requested quantity this layer can fulfill.
     */
    public BigDecimal consumableQuantity(BigDecimal requested) {
        return this.remainingQuantity.min(requested);
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getProductId() { return productId; }
    public Long getContainerId() { return containerId; }
    public String getSerialNumber() { return serialNumber; }
    public BigDecimal getInitialQuantity() { return initialQuantity; }
    public BigDecimal getRemainingQuantity() { return remainingQuantity; }
    public CostAmount getUnitCost() { return unitCost; }
    public ReferenceType getReferenceType() { return referenceType; }
    public Long getReferenceId() { return referenceId; }
    public Long getReferenceLineId() { return referenceLineId; }
    public Long getReversalOfMovementId() { return reversalOfMovementId; }
}
