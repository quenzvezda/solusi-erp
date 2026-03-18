package com.solusi.erp.inventory.service;

import java.math.BigDecimal;

public interface UomConversionService {
    /**
     * Convert a quantity from a source UOM to the product's Base UOM.
     * If sourceUom is already the Base UOM, returns the quantity as is.
     */
    BigDecimal convertToBaseUom(Long productId, Long sourceUomId, BigDecimal quantity);
}
