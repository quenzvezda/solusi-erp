package com.solusi.erp.inventory.model;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.inventory.product.infrastructure.persistence.ProductEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

/**
 * Stock Balance Entity.
 */
@Entity
@Table(name = "inv_stock_balances", uniqueConstraints = {
    @UniqueConstraint(name = "uk_inv_stock_prod_cont_sn", columnNames = {"product_id", "container_id", "serial_number"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockBalance extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "container_id", nullable = false)
    private Container container;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "in_transit_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal inTransitQuantity = BigDecimal.ZERO;
    
    /**
     * Helper to get available quantity.
     * Available = On-Hand - Reserved.
     */
    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }
}
