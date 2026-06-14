package com.solusi.erp.inventory.stock.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.math.BigDecimal;

/**
 * Aggregate Root: StockBalance.
 * Tracks on-hand, reserved, and in-transit quantities for a product at a specific container/serial.
 * Pure Java Domain Model — no Spring, no Lombok, no JPA.
 */
public class StockBalance {

    private final AuditMetadata metadata;
    private final Long productId;
    private final Long containerId;
    private final String serialNumber;
    private BigDecimal quantity;
    private BigDecimal reservedQuantity;
    private BigDecimal inTransitQuantity;

    public StockBalance(AuditMetadata metadata, Long productId, Long containerId, String serialNumber,
                        BigDecimal quantity, BigDecimal reservedQuantity, BigDecimal inTransitQuantity) {
        this.metadata = metadata;
        this.productId = productId;
        this.containerId = containerId;
        this.serialNumber = serialNumber;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.inTransitQuantity = inTransitQuantity;
    }

    public static StockBalance createNew(Long productId, Long containerId, String serialNumber) {
        return new StockBalance(AuditMetadata.empty(), productId, containerId, serialNumber,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    /**
     * Apply a stock movement to this balance.
     * Encapsulates all business rules for how each movement type affects quantities.
     */
    public void applyMovement(MovementType type, BigDecimal qty) {
        switch (type) {
            case RECEIPT, TRANSFER_IN, ADJUSTMENT -> {
                this.quantity = this.quantity.add(qty);
            }
            case ISSUE, TRANSFER_OUT -> {
                this.quantity = this.quantity.subtract(qty);
            }
            case ISSUE_RESERVED -> {
                this.quantity = this.quantity.subtract(qty);
                this.reservedQuantity = this.reservedQuantity.subtract(qty);
            }
            case RESERVE -> {
                this.reservedQuantity = this.reservedQuantity.add(qty);
            }
            case RELEASE -> {
                this.reservedQuantity = this.reservedQuantity.subtract(qty);
            }
        }
    }

    /**
     * Validate business invariants.
     * On-hand quantity and reserved quantity must not be negative.
     * Reserved quantity must not exceed on-hand quantity.
     *
     * @throws IllegalStateException if any invariant is violated
     */
    public void validate() {
        if (this.quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("msg.error.inventory.insufficient_stock");
        }
        if (this.reservedQuantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("msg.error.inventory.insufficient_reserved");
        }
        if (this.reservedQuantity.compareTo(this.quantity) > 0) {
            throw new IllegalStateException("msg.error.inventory.insufficient_available");
        }
    }

    /**
     * Available = On-Hand - Reserved.
     */
    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getProductId() { return productId; }
    public Long getContainerId() { return containerId; }
    public String getSerialNumber() { return serialNumber; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public BigDecimal getInTransitQuantity() { return inTransitQuantity; }
}
