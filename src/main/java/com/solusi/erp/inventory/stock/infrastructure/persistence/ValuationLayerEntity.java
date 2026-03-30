package com.solusi.erp.inventory.stock.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.core.model.CurrencyAmount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Valuation Layer entity for FIFO costing.
 * Tracks the cost of remaining stock layers.
 */
@Entity
@Table(name = "inv_valuation_layers")
@Getter
@Setter
public class ValuationLayerEntity extends BaseModel {

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(name = "initial_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialQuantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal remainingQuantity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "currencyId", column = @Column(name = "unit_cost_currency_id")),
        @AttributeOverride(name = "exchangeRate", column = @Column(name = "unit_cost_exchange_rate")),
        @AttributeOverride(name = "originalAmount", column = @Column(name = "unit_cost_amount_original")),
        @AttributeOverride(name = "localAmount", column = @Column(name = "unit_cost_amount_local"))
    })
    private CurrencyAmount unitCost;
}
