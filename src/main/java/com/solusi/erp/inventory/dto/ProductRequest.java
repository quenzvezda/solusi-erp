package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.validation.ValidUomMeasurement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * DTO for creating or updating a Product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ValidUomMeasurement
public class ProductRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{validation.size}")
    private String code;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 255, message = "{validation.size}")
    private String name;

    @Size(max = 50, message = "{validation.size}")
    private String barcode;

    private String note;

    @NotNull(message = "{validation.notnull}")
    private Long categoryId;

    @NotNull(message = "{validation.notnull}")
    private Long uomId;

    private Long brandId;

    @Size(max = 50, message = "{validation.size}")
    private String hscode;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isSerialized = false;

    @Builder.Default
    private BigDecimal minStock = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal maxStock = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal weightNet = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal weightGross = BigDecimal.ZERO;

    private Long weightUomId;

    @Builder.Default
    private BigDecimal length = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal width = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal height = BigDecimal.ZERO;

    private Long dimensionUomId;
}
