package com.solusi.erp.inventory.uomconversion.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for UomConversion list view.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UomConversionSummaryResponse extends BaseAuditResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private Long fromUomId;
    private String fromUomName;
    private Long toUomId;
    private String toUomName;
    private BigDecimal conversionFactor;
}
