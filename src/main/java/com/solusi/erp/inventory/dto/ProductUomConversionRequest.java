package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for Product UOM Conversion.
 * Cleaned from UI fields to align with Standard API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductUomConversionRequest extends BaseAuditResponse {

    @NotNull(message = "{label.uom-conversion.product} {validation.notnull.suffix}")
    private Long productId;

    @NotNull(message = "{label.uom-conversion.from-uom} {validation.notnull.suffix}")
    private Long fromUomId;

    @NotNull(message = "{label.uom-conversion.to-uom} {validation.notnull.suffix}")
    private Long toUomId; // Base UOM

    @NotNull(message = "{label.uom-conversion.factor} {validation.notnull.suffix}")
    @DecimalMin(value = "0.000001", message = "{label.uom-conversion.factor} {validation.value.positive}")
    @Builder.Default
    private BigDecimal conversionFactor = new BigDecimal("0.00");
}
