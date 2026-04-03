package com.solusi.erp.inventory.uomconversion.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Aggregate Root: UomConversion.
 * Defines the factor to convert from a given UOM to the product's base UOM.
 * 100% Pure Java Domain Model — no Spring, no Lombok.
 */
public class UomConversion {

    private final AuditMetadata metadata;
    private Long productId;
    private String productCode;
    private String productName;
    private Long fromUomId;
    private String fromUomName;
    private Long toUomId;
    private String toUomName;
    private BigDecimal conversionFactor;

    public UomConversion(AuditMetadata metadata,
                         Long productId, String productCode, String productName,
                         Long fromUomId, String fromUomName,
                         Long toUomId, String toUomName,
                         BigDecimal conversionFactor) {
        this.metadata = metadata;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.fromUomId = fromUomId;
        this.fromUomName = fromUomName;
        this.toUomId = toUomId;
        this.toUomName = toUomName;
        this.conversionFactor = conversionFactor;
    }

    public static UomConversion createNew(Long productId, String productCode, String productName,
                                          Long fromUomId, String fromUomName,
                                          Long toUomId, String toUomName,
                                          BigDecimal conversionFactor) {
        if (conversionFactor == null || conversionFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.uom-conversion.invalid-factor");
        }
        return new UomConversion(
            AuditMetadata.empty(),
            productId, productCode, productName,
            fromUomId, fromUomName,
            toUomId, toUomName,
            conversionFactor.setScale(2, RoundingMode.HALF_UP)
        );
    }

    public void update(Long fromUomId, String fromUomName, BigDecimal conversionFactor) {
        if (conversionFactor == null || conversionFactor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("msg.error.uom-conversion.invalid-factor");
        }
        this.fromUomId = fromUomId;
        this.fromUomName = fromUomName;
        this.conversionFactor = conversionFactor.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public Long getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public Long getFromUomId() { return fromUomId; }
    public String getFromUomName() { return fromUomName; }
    public Long getToUomId() { return toUomId; }
    public String getToUomName() { return toUomName; }
    public BigDecimal getConversionFactor() { return conversionFactor; }
}
