package com.solusi.erp.purchasing.supplierpricelist.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SupplierPriceList {

    private final AuditMetadata metadata;
    private final String code;
    private Long supplierId;
    private Long productId;
    private Long uomId;
    private Long currencyId;
    private BigDecimal unitPrice;
    private BigDecimal minQuantity;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String note;
    private boolean active;

    public SupplierPriceList(AuditMetadata metadata, String code, Long supplierId,
                             Long productId, Long uomId, Long currencyId,
                             BigDecimal unitPrice, BigDecimal minQuantity,
                             LocalDate effectiveFrom, LocalDate effectiveTo,
                             String note, boolean active) {
        this.metadata = metadata;
        this.code = code;
        this.supplierId = supplierId;
        this.productId = productId;
        this.uomId = uomId;
        this.currencyId = currencyId;
        this.unitPrice = unitPrice;
        this.minQuantity = minQuantity;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.note = note;
        this.active = active;
    }

    public static SupplierPriceList createNew(String code, Long supplierId,
                                               Long productId, Long uomId, Long currencyId,
                                               BigDecimal unitPrice, BigDecimal minQuantity,
                                               LocalDate effectiveFrom, LocalDate effectiveTo,
                                               String note, boolean active) {
        validateUnitPrice(unitPrice);
        validateDateRange(effectiveFrom, effectiveTo);
        return new SupplierPriceList(AuditMetadata.empty(), code, supplierId,
            productId, uomId, currencyId, unitPrice, minQuantity,
            effectiveFrom, effectiveTo, note, active);
    }

    public void update(Long productId, Long uomId, Long currencyId,
                       Long supplierId,
                       BigDecimal unitPrice, BigDecimal minQuantity,
                       LocalDate effectiveFrom, LocalDate effectiveTo,
                       String note, boolean active) {
        validateUnitPrice(unitPrice);
        validateDateRange(effectiveFrom, effectiveTo);
        this.supplierId = supplierId;
        this.productId = productId;
        this.uomId = uomId;
        this.currencyId = currencyId;
        this.unitPrice = unitPrice;
        this.minQuantity = minQuantity;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.note = note;
        this.active = active;
    }

    public void deactivate() {
        this.active = false;
    }

    private static void validateUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.spl.price.positive");
        }
    }

    private static void validateDateRange(LocalDate effectiveFrom, LocalDate effectiveTo) {
        if (effectiveTo != null && effectiveFrom.isAfter(effectiveTo)) {
            throw new DomainException("msg.error.spl.date.range");
        }
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public Long getSupplierId() { return supplierId; }
    public Long getProductId() { return productId; }
    public Long getUomId() { return uomId; }
    public Long getCurrencyId() { return currencyId; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getMinQuantity() { return minQuantity; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public String getNote() { return note; }
    public boolean isActive() { return active; }
}
