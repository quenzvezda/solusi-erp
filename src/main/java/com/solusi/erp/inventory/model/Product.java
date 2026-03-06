package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Product Entity.
 * Mandate: AGENTS.md Section 4 (Auditing via BaseModel)
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 50)
    private String barcode;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uom_id", nullable = false)
    private UnitOfMeasure uom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @Column(length = 50)
    private String hscode;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_serialized", nullable = false)
    private Boolean isSerialized = false;

    @Column(name = "min_stock", precision = 19, scale = 4)
    private BigDecimal minStock = BigDecimal.ZERO;

    @Column(name = "max_stock", precision = 19, scale = 4)
    private BigDecimal maxStock = BigDecimal.ZERO;

    @Column(name = "weight_net", precision = 19, scale = 4)
    private BigDecimal weightNet = BigDecimal.ZERO;

    @Column(name = "weight_gross", precision = 19, scale = 4)
    private BigDecimal weightGross = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "weight_uom_id")
    private UnitOfMeasure weightUom;

    @Column(name = "dim_length", precision = 19, scale = 4)
    private BigDecimal length = BigDecimal.ZERO;

    @Column(name = "dim_width", precision = 19, scale = 4)
    private BigDecimal width = BigDecimal.ZERO;

    @Column(name = "dim_height", precision = 19, scale = 4)
    private BigDecimal height = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dim_uom_id")
    private UnitOfMeasure dimensionUom;
}
