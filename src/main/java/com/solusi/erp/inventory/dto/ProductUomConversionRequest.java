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
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProductUomConversionRequest extends BaseAuditResponse {

    @NotNull(message = "{validation.notnull}")
    private Long productId;
    
    // For display in UI lookup
    private String productName;
    private String productCode;

    @NotNull(message = "{validation.notnull}")
    private Long fromUomId;
    
    private String fromUomName;

    @NotNull(message = "{validation.notnull}")
    private Long toUomId; // Base UOM
    
    private String toUomName;

    @NotNull(message = "{validation.notnull}")
    @DecimalMin(value = "0.000001", message = "{validation.min}")
    @Builder.Default
    private BigDecimal conversionFactor = new BigDecimal("1.00");
}
