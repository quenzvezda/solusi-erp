package com.solusi.erp.accountspayable.vendorbill.domain.model;

import java.math.BigDecimal;

public final class VendorBillLine {

    private final Long id;
    private final Long grLineId;
    private final Long productId;
    private final String productName;
    private final String description;
    private final BigDecimal qtyBilled;
    private final Long uomId;
    private final String uomName;
    private final BigDecimal unitPrice;
    private final BigDecimal inventoryAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal lineTotal;

    public VendorBillLine(Long id, Long grLineId, Long productId, String productName, String description,
                          BigDecimal qtyBilled, Long uomId, String uomName, BigDecimal unitPrice,
                          BigDecimal inventoryAmount, BigDecimal taxAmount, BigDecimal lineTotal) {
        this.id = id;
        this.grLineId = grLineId;
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.qtyBilled = qtyBilled;
        this.uomId = uomId;
        this.uomName = uomName;
        this.unitPrice = unitPrice;
        this.inventoryAmount = inventoryAmount;
        this.taxAmount = taxAmount;
        this.lineTotal = lineTotal;
    }

    public Long getId() {
        return id;
    }

    public Long getGrLineId() {
        return grLineId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getQtyBilled() {
        return qtyBilled;
    }

    public Long getUomId() {
        return uomId;
    }

    public String getUomName() {
        return uomName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getInventoryAmount() {
        return inventoryAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
