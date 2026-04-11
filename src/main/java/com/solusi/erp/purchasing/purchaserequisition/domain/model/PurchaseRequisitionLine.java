package com.solusi.erp.purchasing.purchaserequisition.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PurchaseRequisitionLine {

    private final AuditMetadata metadata;
    private Long headerId;
    private final Long productId;
    private final BigDecimal quantity;
    private final Long uomId;
    private final LocalDate requiredDate;
    private final BigDecimal estimatedUnitPrice;
    private final Long suggestedSupplierId;
    private final Long convertedPoLineId;
    private final String note;

    public PurchaseRequisitionLine(AuditMetadata metadata, Long headerId,
                                    Long productId, BigDecimal quantity, Long uomId,
                                    LocalDate requiredDate, BigDecimal estimatedUnitPrice,
                                    Long suggestedSupplierId, Long convertedPoLineId,
                                    String note) {
        validateQuantity(quantity);
        this.metadata = metadata;
        this.headerId = headerId;
        this.productId = productId;
        this.quantity = quantity;
        this.uomId = uomId;
        this.requiredDate = requiredDate;
        this.estimatedUnitPrice = estimatedUnitPrice;
        this.suggestedSupplierId = suggestedSupplierId;
        this.convertedPoLineId = convertedPoLineId;
        this.note = note;
    }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.pr.line.quantity.positive");
        }
    }

    public void setHeaderId(Long headerId) {
        this.headerId = headerId;
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getHeaderId() { return headerId; }
    public Long getProductId() { return productId; }
    public BigDecimal getQuantity() { return quantity; }
    public Long getUomId() { return uomId; }
    public LocalDate getRequiredDate() { return requiredDate; }
    public BigDecimal getEstimatedUnitPrice() { return estimatedUnitPrice; }
    public Long getSuggestedSupplierId() { return suggestedSupplierId; }
    public Long getConvertedPoLineId() { return convertedPoLineId; }
    public String getNote() { return note; }
}
