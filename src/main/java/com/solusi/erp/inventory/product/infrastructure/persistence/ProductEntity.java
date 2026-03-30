package com.solusi.erp.inventory.product.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

/**
 * JPA Entity for Product.
 * Part of Infrastructure Layer.
 */
@Entity
@Table(name = "products")
@Getter
@Setter
public class ProductEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 50)
    private String barcode;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "brand_id")
    private Long brandId;

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

    @Column(name = "weight_uom_id")
    private Long weightUomId;

    @Column(name = "dim_length", precision = 19, scale = 4)
    private BigDecimal length = BigDecimal.ZERO;

    @Column(name = "dim_width", precision = 19, scale = 4)
    private BigDecimal width = BigDecimal.ZERO;

    @Column(name = "dim_height", precision = 19, scale = 4)
    private BigDecimal height = BigDecimal.ZERO;

    @Column(name = "dim_uom_id")
    private Long dimensionUomId;
}
