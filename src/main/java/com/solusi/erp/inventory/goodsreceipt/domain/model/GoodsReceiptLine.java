package com.solusi.erp.inventory.goodsreceipt.domain.model;

import java.math.BigDecimal;

public final class GoodsReceiptLine {

    private final Long poLineId;
    private final Long productId;
    private final Long sourceFacilityId;
    private final Boolean serialized;
    private final BigDecimal quantityReceived;
    private final Long uomId;
    private final Long containerId;
    private final BigDecimal unitPrice;
    private final BigDecimal baseQuantity;
    private final BigDecimal inventoryAmount;
    private final BigDecimal taxBaseAmount;
    private final BigDecimal taxAmount;
    private final BigDecimal grIrAmount;
    private final String serialNumber;

    private GoodsReceiptLine(Long poLineId, Long productId, Long sourceFacilityId, Boolean serialized,
                             BigDecimal quantityReceived, Long uomId, Long containerId,
                             BigDecimal unitPrice, BigDecimal baseQuantity, BigDecimal inventoryAmount,
                             BigDecimal taxBaseAmount, BigDecimal taxAmount, BigDecimal grIrAmount,
                             String serialNumber) {
        this.poLineId = poLineId;
        this.productId = productId;
        this.sourceFacilityId = sourceFacilityId;
        this.serialized = serialized;
        this.quantityReceived = quantityReceived;
        this.uomId = uomId;
        this.containerId = containerId;
        this.unitPrice = unitPrice;
        this.baseQuantity = baseQuantity;
        this.inventoryAmount = inventoryAmount;
        this.taxBaseAmount = taxBaseAmount;
        this.taxAmount = taxAmount;
        this.grIrAmount = grIrAmount;
        this.serialNumber = serialNumber;
    }

    public static GoodsReceiptLine prefill(Long poLineId, Long productId, Long sourceFacilityId, Boolean serialized,
                                           BigDecimal quantityReceived, Long uomId, Long containerId,
                                           BigDecimal unitPrice, BigDecimal baseQuantity, BigDecimal inventoryAmount,
                                           BigDecimal taxBaseAmount, BigDecimal grIrAmount, String serialNumber) {
        return new GoodsReceiptLine(
                poLineId, productId, sourceFacilityId, serialized, quantityReceived, uomId,
                containerId, unitPrice, baseQuantity, inventoryAmount, taxBaseAmount, BigDecimal.ZERO,
                grIrAmount, serialNumber
        );
    }

    public static GoodsReceiptLine prefill(Long poLineId, Long productId, Long sourceFacilityId, Boolean serialized,
                                           BigDecimal quantityReceived, Long uomId, Long containerId,
                                           BigDecimal unitPrice, BigDecimal baseQuantity, BigDecimal inventoryAmount,
                                           BigDecimal taxBaseAmount, BigDecimal taxAmount, BigDecimal grIrAmount,
                                           String serialNumber) {
        return new GoodsReceiptLine(
                poLineId, productId, sourceFacilityId, serialized, quantityReceived, uomId,
                containerId, unitPrice, baseQuantity, inventoryAmount, taxBaseAmount, taxAmount,
                grIrAmount, serialNumber
        );
    }

    public boolean hasReceiptQuantity() {
        return quantityReceived != null && quantityReceived.compareTo(BigDecimal.ZERO) > 0;
    }

    public Long getPoLineId() {
        return poLineId;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getSourceFacilityId() {
        return sourceFacilityId;
    }

    public Boolean getSerialized() {
        return serialized;
    }

    public BigDecimal getQuantityReceived() {
        return quantityReceived;
    }

    public Long getUomId() {
        return uomId;
    }

    public Long getContainerId() {
        return containerId;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getBaseQuantity() {
        return baseQuantity;
    }

    public BigDecimal getInventoryAmount() {
        return inventoryAmount;
    }

    public BigDecimal getTaxBaseAmount() {
        return taxBaseAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public BigDecimal getGrIrAmount() {
        return grIrAmount;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}
