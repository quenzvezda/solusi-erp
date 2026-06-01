package com.solusi.erp.inventory.goodsissue.infrastructure.persistence;

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
@Table(name = "inv_goods_issue_lines")
@Getter @Setter @NoArgsConstructor
public class GoodsIssueLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private GoodsIssueEntity header;

    @Column(name = "reference_line_id")
    private Long referenceLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "is_serialized", nullable = false)
    private Boolean serialized = Boolean.FALSE;

    @Column(name = "quantity_issued", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityIssued;

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

    @Column(name = "serial_number")
    private String serialNumber;

    @Column(name = "unit_cost", precision = 19, scale = 6)
    private BigDecimal unitCost;

    @Column(name = "inventory_amount", precision = 19, scale = 4)
    private BigDecimal inventoryAmount;

    @Column(name = "tax_base_amount", precision = 19, scale = 4)
    private BigDecimal taxBaseAmount;

    @Column(name = "tax_amount", precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "clearing_amount", precision = 19, scale = 4)
    private BigDecimal clearingAmount;

    @Column(name = "valuation_ref_type", length = 50)
    private String valuationRefType;

    @Column(name = "valuation_ref_id")
    private Long valuationRefId;

    @Column(name = "valuation_ref_line_id")
    private Long valuationRefLineId;
}
