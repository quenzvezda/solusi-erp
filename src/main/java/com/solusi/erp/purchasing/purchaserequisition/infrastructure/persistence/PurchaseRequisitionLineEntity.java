package com.solusi.erp.purchasing.purchaserequisition.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pur_purchase_requisition_lines")
@Getter
@Setter
@NoArgsConstructor
public class PurchaseRequisitionLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private PurchaseRequisitionEntity header;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "required_date")
    private LocalDate requiredDate;

    @Column(name = "estimated_unit_price", precision = 19, scale = 4)
    private BigDecimal estimatedUnitPrice;

    @Column(name = "suggested_supplier_id")
    private Long suggestedSupplierId;

    @Column(name = "converted_po_line_id")
    private Long convertedPoLineId;

    @Column(columnDefinition = "TEXT")
    private String note;
}
