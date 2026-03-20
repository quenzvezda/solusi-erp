package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for Product UOM Conversion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductUomConversionResponse extends BaseAuditResponse {
    private Long productId;
    private String productCode;
    private String productName;

    private Long fromUomId;
    private String fromUomName;

    private Long toUomId;
    private String toUomName;

    private BigDecimal conversionFactor;
}
