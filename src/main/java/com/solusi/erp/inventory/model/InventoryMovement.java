package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.core.model.CurrencyAmount;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Inventory Movement Entity.
 */
@Entity
@Table(name = "inv_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement extends BaseModel {

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private MovementType movementType;

    @Enumerated(EnumType.STRING)
    @Column(name = "reference_type", length = 50)
    private ReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "currencyId", column = @Column(name = "unit_cost_currency_id")),
        @AttributeOverride(name = "exchangeRate", column = @Column(name = "unit_cost_exchange_rate")),
        @AttributeOverride(name = "originalAmount", column = @Column(name = "unit_cost_amount_original")),
        @AttributeOverride(name = "localAmount", column = @Column(name = "unit_cost_amount_local"))
    })
    private CurrencyAmount unitCost;
}
