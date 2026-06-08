package com.solusi.erp.purchasing.purchasereturn.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public final class PurchaseReturnReversalLine {

    private final AuditMetadata metadata;
    private final Long purchaseReturnLineId;
    private final Long originalMovementId;
    private final Long targetContainerId;
    private final Long productId;
    private final String serialNumber;
    private final BigDecimal quantity;

    private PurchaseReturnReversalLine(AuditMetadata metadata,
                                       Long purchaseReturnLineId,
                                       Long originalMovementId,
                                       Long targetContainerId,
                                       Long productId,
                                       String serialNumber,
                                       BigDecimal quantity) {
        this.metadata = metadata == null ? AuditMetadata.empty() : metadata;
        this.purchaseReturnLineId = purchaseReturnLineId;
        this.originalMovementId = originalMovementId;
        this.targetContainerId = targetContainerId;
        this.productId = productId;
        this.serialNumber = serialNumber;
        this.quantity = quantity;
    }

    public static PurchaseReturnReversalLine create(Long purchaseReturnLineId,
                                                    Long originalMovementId,
                                                    Long targetContainerId,
                                                    Long productId,
                                                    String serialNumber,
                                                    BigDecimal quantity) {
        validate(originalMovementId, targetContainerId, productId, quantity);
        return new PurchaseReturnReversalLine(
                AuditMetadata.empty(), purchaseReturnLineId, originalMovementId,
                targetContainerId, productId, serialNumber, quantity);
    }

    public static PurchaseReturnReversalLine reconstitute(AuditMetadata metadata,
                                                          Long purchaseReturnLineId,
                                                          Long originalMovementId,
                                                          Long targetContainerId,
                                                          Long productId,
                                                          String serialNumber,
                                                          BigDecimal quantity) {
        return new PurchaseReturnReversalLine(
                metadata, purchaseReturnLineId, originalMovementId, targetContainerId,
                productId, serialNumber, quantity);
    }

    private static void validate(Long originalMovementId,
                                 Long targetContainerId,
                                 Long productId,
                                 BigDecimal quantity) {
        if (originalMovementId == null) {
            throw new DomainException("msg.error.purchase-return.reverse.original-movement-required");
        }
        if (targetContainerId == null) {
            throw new DomainException("msg.error.purchase-return.reverse.target-container-required");
        }
        if (productId == null) {
            throw new DomainException("msg.error.purchase-return.reverse.product-required");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.purchase-return.reverse.quantity-positive");
        }
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getPurchaseReturnLineId() { return purchaseReturnLineId; }
    public Long getOriginalMovementId() { return originalMovementId; }
    public Long getTargetContainerId() { return targetContainerId; }
    public Long getProductId() { return productId; }
    public String getSerialNumber() { return serialNumber; }
    public BigDecimal getQuantity() { return quantity; }
}
