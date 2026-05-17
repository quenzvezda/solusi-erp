package com.solusi.erp.accountspayable.vendorbill.infrastructure.persistence;

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
@Table(name = "ap_vendor_bill_lines")
@Getter @Setter @NoArgsConstructor
public class VendorBillLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private VendorBillEntity bill;

    @Column(name = "gr_line_id", nullable = false)
    private Long grLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(length = 500)
    private String description;

    @Column(name = "qty_billed", nullable = false, precision = 19, scale = 4)
    private BigDecimal qtyBilled;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "uom_name", nullable = false, length = 100)
    private String uomName;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "inventory_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal inventoryAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 19, scale = 4)
    private BigDecimal lineTotal = BigDecimal.ZERO;
}
