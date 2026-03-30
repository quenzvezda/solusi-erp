package com.solusi.erp.inventory.uomconversion.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product_uom_conversions")
@Getter
@Setter
public class UomConversionEntity extends BaseModel {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "from_uom_id", nullable = false)
    private Long fromUomId;

    @Column(name = "to_uom_id", nullable = false)
    private Long toUomId;

    @Column(name = "conversion_factor", nullable = false, precision = 19, scale = 6)
    private BigDecimal conversionFactor;
}
