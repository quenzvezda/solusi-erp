package com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pur_purchase_return_reversal_lines")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnReversalLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_return_id", nullable = false)
    private PurchaseReturnEntity purchaseReturn;

    @Column(name = "purchase_return_line_id")
    private Long purchaseReturnLineId;

    @Column(name = "original_movement_id", nullable = false)
    private Long originalMovementId;

    @Column(name = "target_container_id", nullable = false)
    private Long targetContainerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;
}
