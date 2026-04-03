package com.solusi.erp.inventory.adjustment.domain.model;

import java.math.BigDecimal;

/**
 * Value Object representing one line of a StockAdjustment aggregate.
 * Pure Java — no Spring, no Lombok, no JPA.
 */
public class StockAdjustmentLineItem {

    private final Long id;
    private final Integer version;
    private final Long productId;
    private final String productCode;
    private final String productName;
    private final Boolean isSerialized;
    private final Long gridId;
    private final String gridCode;
    private final String gridName;
    private final Long containerId;
    private final String containerCode;
    private final String containerName;
    private final String facilityName;
    private final Long uomId;
    private final String uomName;
    private final BigDecimal conversionFactor;
    private final BigDecimal quantity;
    private final BigDecimal unitCost;
    private final BigDecimal totalAmount;
    private final String serialNumber;

    public StockAdjustmentLineItem(Long id, Integer version, Long productId, String productCode, String productName,
                                   Boolean isSerialized, Long gridId, String gridCode, String gridName,
                                   Long containerId, String containerCode, String containerName, String facilityName,
                                   Long uomId, String uomName, BigDecimal conversionFactor,
                                   BigDecimal quantity, BigDecimal unitCost, BigDecimal totalAmount, String serialNumber) {
        this.id = id;
        this.version = version;
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.isSerialized = isSerialized;
        this.gridId = gridId;
        this.gridCode = gridCode;
        this.gridName = gridName;
        this.containerId = containerId;
        this.containerCode = containerCode;
        this.containerName = containerName;
        this.facilityName = facilityName;
        this.uomId = uomId;
        this.uomName = uomName;
        this.conversionFactor = conversionFactor;
        this.quantity = quantity;
        this.unitCost = unitCost;
        this.totalAmount = totalAmount;
        this.serialNumber = serialNumber;
    }

    public StockAdjustmentLineItem updateSerialNumbers(String newSerialNumber) {
        return new StockAdjustmentLineItem(id, version, productId, productCode, productName, isSerialized,
                gridId, gridCode, gridName, containerId, containerCode, containerName, facilityName,
                uomId, uomName, conversionFactor, quantity, unitCost, totalAmount, newSerialNumber);
    }

    public Long getId() { return id; }
    public Integer getVersion() { return version; }
    public Long getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public String getProductName() { return productName; }
    public Boolean getIsSerialized() { return isSerialized; }
    public Long getGridId() { return gridId; }
    public String getGridCode() { return gridCode; }
    public String getGridName() { return gridName; }
    public Long getContainerId() { return containerId; }
    public String getContainerCode() { return containerCode; }
    public String getContainerName() { return containerName; }
    public String getFacilityName() { return facilityName; }
    public Long getUomId() { return uomId; }
    public String getUomName() { return uomName; }
    public BigDecimal getConversionFactor() { return conversionFactor; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getSerialNumber() { return serialNumber; }
}
