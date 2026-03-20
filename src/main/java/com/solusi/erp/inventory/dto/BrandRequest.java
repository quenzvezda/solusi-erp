package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Brand.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BrandRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{label.brand.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.brand.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.brand.name} {validation.size.suffix}")
    private String name;

    private String note;
}
