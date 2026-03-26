package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.core.model.CurrencyAmount;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.AssociationOverride;
import jakarta.persistence.AssociationOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
 * Valuation Layer entity for FIFO costing.
 * Tracks the cost of remaining stock layers.
 */
@Entity
@Table(name = "inv_valuation_layers")
@Getter
@Setter
public class ValuationLayer extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "initial_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialQuantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingQuantity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "exchangeRate", column = @Column(name = "unit_cost_exchange_rate")),
        @AttributeOverride(name = "originalAmount", column = @Column(name = "unit_cost_amount_original")),
        @AttributeOverride(name = "localAmount", column = @Column(name = "unit_cost_amount_local"))
    })
    @AssociationOverrides({
        @AssociationOverride(name = "currency", joinColumns = @JoinColumn(name = "unit_cost_currency_id"))
    })
    private CurrencyAmount unitCost;
}
