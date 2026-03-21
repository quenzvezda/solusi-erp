package com.solusi.erp.master.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating or updating a Tax.
 * Following project standard: extends BaseAuditResponse.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaxRequest extends BaseAuditResponse {

    @NotBlank(message = "{tax.code.required}")
    private String code;

    @NotBlank(message = "{tax.name.required}")
    private String name;

    @NotNull(message = "{tax.rate.required}")
    @DecimalMin(value = "0.0", message = "{tax.rate.min}")
    private BigDecimal rate;

    private String note;

    @Builder.Default
    private Boolean isSubtract = false;

    @Builder.Default
    private Boolean isActive = true;
}
