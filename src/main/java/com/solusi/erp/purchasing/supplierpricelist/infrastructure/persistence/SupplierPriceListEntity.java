package com.solusi.erp.purchasing.supplierpricelist.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pur_supplier_price_lists")
@Getter
@Setter
@NoArgsConstructor
public class SupplierPriceListEntity extends BaseModel {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "currency_id", nullable = false)
    private Long currencyId;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "min_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal minQuantity;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "is_active", nullable = false)
    private boolean active;
}
