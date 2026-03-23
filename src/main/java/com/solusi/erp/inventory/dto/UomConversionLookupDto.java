package com.solusi.erp.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO for UOM Conversion selection in transaction forms.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UomConversionLookupDto {
    private Long uomId;
    private String uomName;
    private String uomCode;
    private BigDecimal conversionFactor;
    private Boolean isBase;
}
