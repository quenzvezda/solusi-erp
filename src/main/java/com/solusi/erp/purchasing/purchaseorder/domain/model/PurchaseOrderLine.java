package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;

public class PurchaseOrderLine {

    private final AuditMetadata metadata;
    private Long headerId;
    private final Long productId;
    private final BigDecimal quantity;
    private final BigDecimal receivedQuantity;
    private final Long uomId;
    private final BigDecimal unitPrice;
    private final BigDecimal taxRate;
    private final BigDecimal lineSubtotal;
    private final BigDecimal lineTax;
    private final BigDecimal lineTotal;
    private final Long prLineId;
    private final String note;

    public PurchaseOrderLine(AuditMetadata metadata, Long headerId,
                              Long productId, BigDecimal quantity,
                              BigDecimal receivedQuantity, Long uomId,
                              BigDecimal unitPrice, BigDecimal taxRate,
                              Long prLineId, String note) {
        validateQuantity(quantity);
        validateUnitPrice(unitPrice);
        this.metadata = metadata;
        this.headerId = headerId;
        this.productId = productId;
        this.quantity = quantity;
        this.receivedQuantity = receivedQuantity != null ? receivedQuantity : BigDecimal.ZERO;
        this.uomId = uomId;
        this.unitPrice = unitPrice;
        this.taxRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        this.prLineId = prLineId;
        this.note = note;

        // Calculate derived fields
        this.lineSubtotal = this.quantity.multiply(this.unitPrice);
        this.lineTax = this.lineSubtotal.multiply(this.taxRate);
        this.lineTotal = this.lineSubtotal.add(this.lineTax);
    }

    private static void validateQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.po.line.quantity.positive");
        }
    }

    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.po.line.unitprice.positive");
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
    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public Long getUomId() { return uomId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTaxRate() { return taxRate; }
    public BigDecimal getLineSubtotal() { return lineSubtotal; }
    public BigDecimal getLineTax() { return lineTax; }
    public BigDecimal getLineTotal() { return lineTotal; }
    public Long getPrLineId() { return prLineId; }
    public String getNote() { return note; }
}
