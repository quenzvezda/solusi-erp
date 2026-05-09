package com.solusi.erp.inventory.goodsreceipt.infrastructure.persistence;

import com.solusi.erp.core.model.BaseModel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "pur_goods_receipt_lines")
@Getter @Setter @NoArgsConstructor
public class GoodsReceiptLineEntity extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "header_id", nullable = false)
    private GoodsReceiptEntity header;

    @Column(name = "reference_line_id", nullable = false)
    private Long referenceLineId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "is_serialized", nullable = false)
    private Boolean serialized = Boolean.FALSE;

    @Column(name = "quantity_received", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityReceived;

    @Column(name = "uom_id", nullable = false)
    private Long uomId;

    @Column(name = "container_id")
    private Long containerId;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "base_quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal baseQuantity;

    @Column(name = "inventory_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal inventoryAmount;

    @Column(name = "tax_base_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxBaseAmount;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "gr_ir_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal grIrAmount;

    @Column(name = "serial_number", length = 100)
    private String serialNumber;
}
