package com.solusi.erp.inventory.uomconversion.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a UomConversion.
 */
@Data
@NoArgsConstructor
public class UomConversionSaveRequest extends BaseAuditResponse {

    @NotNull(message = "{label.uom-conversion.product} {validation.notblank.suffix}")
    private Long productId;

    @NotNull(message = "{label.uom-conversion.from-uom} {validation.notblank.suffix}")
    private Long fromUomId;

    private Long toUomId;

    @NotNull(message = "{label.uom-conversion.factor} {validation.notblank.suffix}")
    @DecimalMin(value = "0.01", message = "{label.uom-conversion.factor} {validation.min.suffix}")
    private BigDecimal conversionFactor;
}
