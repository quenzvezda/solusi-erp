package com.solusi.erp.purchasing.purchasereturn.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import com.solusi.erp.purchasing.purchasereturn.domain.model.PurchaseReturnReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pur_purchase_return_lines")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseReturnLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private PurchaseReturnEntity header;

    @Column(name = "goods_receipt_line_id", nullable = false)
    private Long goodsReceiptLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "is_serialized", nullable = false)
    private boolean serialized;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "base_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseQuantity;

    @Column(name = "facility_id", nullable = false)
    private Long facilityId;

    @Column(name = "grid_id", nullable = false)
    private Long gridId;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Column(name = "serial_numbers", length = 1000)
    private String serialNumbers;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false, length = 40)
    private PurchaseReturnReason reason;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "valuation_ref_type", nullable = false, length = 50)
    private String valuationReferenceType;

    @Column(name = "valuation_ref_id", nullable = false)
    private Long valuationReferenceId;

    @Column(name = "valuation_ref_line_id", nullable = false)
    private Long valuationReferenceLineId;

    @Column(name = "unit_cost", nullable = false, precision = 19, scale = 6)
    private BigDecimal unitCost;

    @Column(name = "inventory_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal inventoryAmount;

    @Column(name = "tax_reversal_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxReversalAmount;

    @Column(name = "clearing_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal clearingAmount;
}
