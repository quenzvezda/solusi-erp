package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;

/**
 * Product UOM Conversion entity.
 * Defines factors to convert units to the product's Base UOM.
 */
@Entity
@Table(name = "product_uom_conversions")
@Getter
@Setter
public class ProductUomConversion extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_uom_id", nullable = false)
    private UnitOfMeasure fromUom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_uom_id", nullable = false)
    private UnitOfMeasure toUom;

    @Column(name = "conversion_factor", nullable = false, precision = 19, scale = 6)
    private BigDecimal conversionFactor;
}
