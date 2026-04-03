package com.solusi.erp.inventory.uomconversion.web.dto;

import java.math.BigDecimal;

/**
 * Lookup DTO for UOM conversion AJAX endpoint.
 * Includes the base UOM and all conversion UOMs for a product.
 */
public record UomConversionLookupData(
    Long uomId,
    String uomName,
    String uomCode,
    BigDecimal conversionFactor,
    Boolean isBase
) {}
