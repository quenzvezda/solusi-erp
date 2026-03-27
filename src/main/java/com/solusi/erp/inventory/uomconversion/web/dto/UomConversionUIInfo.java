package com.solusi.erp.inventory.uomconversion.web.dto;

/**
 * UI display info for UomConversion edit form (read-only display fields).
 */
public class UomConversionUIInfo {

    private final String productName;
    private final String productCode;
    private final String toUomName;

    public UomConversionUIInfo(String productName, String productCode, String toUomName) {
        this.productName = productName;
        this.productCode = productCode;
        this.toUomName = toUomName;
    }

    public String getProductName() { return productName; }
    public String getProductCode() { return productCode; }
    public String getToUomName() { return toUomName; }
}
