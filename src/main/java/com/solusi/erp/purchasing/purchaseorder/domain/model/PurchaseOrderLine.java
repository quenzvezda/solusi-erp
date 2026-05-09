package com.solusi.erp.purchasing.purchaseorder.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.tax.domain.model.TaxCalculationMode;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PurchaseOrderLine {

    private final AuditMetadata metadata;
    private Long headerId;
    private final Long productId;
    private final BigDecimal quantity;
    private BigDecimal receivedQuantity;
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

    private PurchaseOrderLine(AuditMetadata metadata, Long headerId,
                              Long productId, BigDecimal quantity,
                              BigDecimal receivedQuantity, Long uomId,
                              BigDecimal unitPrice, BigDecimal taxRate,
                              BigDecimal lineSubtotal, BigDecimal lineTax, BigDecimal lineTotal,
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
        this.lineSubtotal = lineSubtotal != null ? lineSubtotal : BigDecimal.ZERO;
        this.lineTax = lineTax != null ? lineTax : BigDecimal.ZERO;
        this.lineTotal = lineTotal != null ? lineTotal : BigDecimal.ZERO;
        this.prLineId = prLineId;
        this.note = note;
    }

    public static PurchaseOrderLine rehydrate(AuditMetadata metadata, Long headerId, Long productId,
                                              BigDecimal quantity, BigDecimal receivedQuantity, Long uomId,
                                              BigDecimal unitPrice, BigDecimal taxRate,
                                              BigDecimal lineSubtotal, BigDecimal lineTax, BigDecimal lineTotal,
                                              Long prLineId, String note) {
        return new PurchaseOrderLine(
                metadata, headerId, productId, quantity, receivedQuantity, uomId,
                unitPrice, taxRate, lineSubtotal, lineTax, lineTotal, prLineId, note
        );
    }

    public PurchaseOrderLine recalculate(BigDecimal taxRate, TaxCalculationMode mode) {
        BigDecimal effectiveRate = taxRate != null ? taxRate : BigDecimal.ZERO;
        TaxCalculationMode effectiveMode = mode != null ? mode : TaxCalculationMode.EXCLUSIVE;
        BigDecimal gross = quantity.multiply(unitPrice);

        if (effectiveMode == TaxCalculationMode.INCLUSIVE && effectiveRate.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal base = gross.divide(BigDecimal.ONE.add(effectiveRate), 4, RoundingMode.HALF_UP);
            BigDecimal tax = gross.subtract(base).setScale(4, RoundingMode.HALF_UP);
            return new PurchaseOrderLine(
                    metadata, headerId, productId, quantity, receivedQuantity, uomId,
                    unitPrice, effectiveRate,
                    base, tax, gross.setScale(4, RoundingMode.HALF_UP),
                    prLineId, note
            );
        }

        BigDecimal base = gross.setScale(4, RoundingMode.HALF_UP);
        BigDecimal tax = base.multiply(effectiveRate).setScale(4, RoundingMode.HALF_UP);
        return new PurchaseOrderLine(
                metadata, headerId, productId, quantity, receivedQuantity, uomId,
                unitPrice, effectiveRate,
                base, tax, base.add(tax).setScale(4, RoundingMode.HALF_UP),
                prLineId, note
        );
    }

    public BigDecimal getOutstandingQuantity() {
        return quantity.subtract(receivedQuantity != null ? receivedQuantity : BigDecimal.ZERO);
    }

    public void validateReceipt(BigDecimal qty) {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.gr.line.quantity.positive");
        }
        BigDecimal next = (receivedQuantity != null ? receivedQuantity : BigDecimal.ZERO).add(qty);
        if (next.compareTo(quantity) > 0) {
            throw new DomainException("msg.error.gr.line.exceeds.outstanding");
        }
    }

    public void receive(BigDecimal qty) {
        validateReceipt(qty);
        BigDecimal next = (receivedQuantity != null ? receivedQuantity : BigDecimal.ZERO).add(qty);
        this.receivedQuantity = next;
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
