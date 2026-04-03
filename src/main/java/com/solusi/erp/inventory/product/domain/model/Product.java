package com.solusi.erp.inventory.product.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import java.math.BigDecimal;

/**
 * Aggregate Root: Product.
 * 100% Pure Java Domain Model.
 */
public class Product {
    private final AuditMetadata metadata;
    private String code;
    private String name;
    private String barcode;
    private String note;
    private Long categoryId;
    private Long uomId;
    private Long brandId;
    private String hscode;
    private boolean isActive;
    private boolean isSerialized;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private BigDecimal weightNet;
    private BigDecimal weightGross;
    private Long weightUomId;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Long dimensionUomId;

    public Product(
        AuditMetadata metadata,
        String code,
        String name,
        String barcode,
        String note,
        Long categoryId,
        Long uomId,
        Long brandId,
        String hscode,
        boolean isActive,
        boolean isSerialized,
        BigDecimal minStock,
        BigDecimal maxStock,
        BigDecimal weightNet,
        BigDecimal weightGross,
        Long weightUomId,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        Long dimensionUomId
    ) {
        this.metadata = metadata;
        this.code = code;
        this.name = name;
        this.barcode = barcode;
        this.note = note;
        this.categoryId = categoryId;
        this.uomId = uomId;
        this.brandId = brandId;
        this.hscode = hscode;
        this.isActive = isActive;
        this.isSerialized = isSerialized;
        this.minStock = minStock != null ? minStock : BigDecimal.ZERO;
        this.maxStock = maxStock != null ? maxStock : BigDecimal.ZERO;
        this.weightNet = weightNet != null ? weightNet : BigDecimal.ZERO;
        this.weightGross = weightGross != null ? weightGross : BigDecimal.ZERO;
        this.weightUomId = weightUomId;
        this.length = length != null ? length : BigDecimal.ZERO;
        this.width = width != null ? width : BigDecimal.ZERO;
        this.height = height != null ? height : BigDecimal.ZERO;
        this.dimensionUomId = dimensionUomId;
    }

    public static Product createNew(
        String code,
        String name,
        String barcode,
        String note,
        Long categoryId,
        Long uomId,
        Long brandId,
        String hscode,
        boolean isActive,
        boolean isSerialized,
        BigDecimal minStock,
        BigDecimal maxStock,
        BigDecimal weightNet,
        BigDecimal weightGross,
        Long weightUomId,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        Long dimensionUomId
    ) {
        return new Product(
            AuditMetadata.empty(),
            code,
            name,
            barcode,
            note,
            categoryId,
            uomId,
            brandId,
            hscode,
            isActive,
            isSerialized,
            minStock,
            maxStock,
            weightNet,
            weightGross,
            weightUomId,
            length,
            width,
            height,
            dimensionUomId
        );
    }

    // Business Logic Methods can be added here
    
    public void updateInfo(String name, String barcode, String note, Long categoryId, Long uomId, Long brandId, String hscode) {
        this.name = name;
        this.barcode = barcode;
        this.note = note;
        this.categoryId = categoryId;
        this.uomId = uomId;
        this.brandId = brandId;
        this.hscode = hscode;
    }

    public void updateStatus(boolean isActive) {
        this.isActive = isActive;
    }

    // Getters
    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getBarcode() { return barcode; }
    public String getNote() { return note; }
    public Long getCategoryId() { return categoryId; }
    public Long getUomId() { return uomId; }
    public Long getBrandId() { return brandId; }
    public String getHscode() { return hscode; }
    public boolean isActive() { return isActive; }
    public boolean isSerialized() { return isSerialized; }
    public BigDecimal getMinStock() { return minStock; }
    public BigDecimal getMaxStock() { return maxStock; }
    public BigDecimal getWeightNet() { return weightNet; }
    public BigDecimal getWeightGross() { return weightGross; }
    public Long getWeightUomId() { return weightUomId; }
    public BigDecimal getLength() { return length; }
    public BigDecimal getWidth() { return width; }
    public BigDecimal getHeight() { return height; }
    public Long getDimensionUomId() { return dimensionUomId; }
}
