package com.solusi.erp.inventory.adjustment.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "inv_stock_adjustment_lines")
@Getter
@Setter
@NoArgsConstructor
public class StockAdjustmentLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private StockAdjustmentEntity header;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "grid_id")
    private Long gridId;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Column(name = "uom_id")
    private Long uomId;

    @Column(name = "conversion_factor", precision = 19, scale = 4)
    private BigDecimal conversionFactor;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;
}
