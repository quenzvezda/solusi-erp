package com.solusi.erp.inventory.product.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.product.web.validation.ValidUomMeasurement;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * Intent-Based DTO for Saving (Create/Update) Product.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ValidUomMeasurement
public class ProductSaveRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{label.product.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.product.name} {validation.notblank.suffix}")
    @Size(max = 255, message = "{label.product.name} {validation.size.suffix}")
    private String name;

    @Size(max = 50, message = "{label.product.barcode} {validation.size.suffix}")
    private String barcode;

    private String note;

    @NotNull(message = "{label.product.category} {validation.notnull.suffix}")
    private Long categoryId;

    // Display-only fields for TomSelect label persistence (AGENTS.md §6)
    private String categoryName;
    private String categoryCode;
    private String categoryType;

    @NotNull(message = "{label.product.uom} {validation.notnull.suffix}")
    private Long uomId;

    private Long brandId;

    // Display-only fields for TomSelect label persistence (AGENTS.md §6)
    private String brandName;
    private String brandCode;

    @Size(max = 50, message = "{label.product.hscode} {validation.size.suffix}")
    private String hscode;

    private Boolean isActive = true;

    private Boolean isSerialized = false;

    private BigDecimal minStock = BigDecimal.ZERO;

    private BigDecimal maxStock = BigDecimal.ZERO;

    private BigDecimal weightNet = BigDecimal.ZERO;

    private BigDecimal weightGross = BigDecimal.ZERO;

    private Long weightUomId;

    private BigDecimal length = BigDecimal.ZERO;

    private BigDecimal width = BigDecimal.ZERO;

    private BigDecimal height = BigDecimal.ZERO;

    private Long dimensionUomId;
}
