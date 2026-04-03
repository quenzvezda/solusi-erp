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

    public ValuationLayer(AuditMetadata metadata, Long productId, Long containerId, String serialNumber,
                          BigDecimal initialQuantity, BigDecimal remainingQuantity, CostAmount unitCost) {
        this.metadata = metadata;
        this.productId = productId;
        this.containerId = containerId;
        this.serialNumber = serialNumber;
        this.initialQuantity = initialQuantity;
        this.remainingQuantity = remainingQuantity;
        this.unitCost = unitCost;
    }

    public static ValuationLayer createNew(Long productId, Long containerId, String serialNumber,
                                           BigDecimal quantity, CostAmount unitCost) {
        return new ValuationLayer(AuditMetadata.empty(), productId, containerId, serialNumber,
                quantity, quantity, unitCost);
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
}
