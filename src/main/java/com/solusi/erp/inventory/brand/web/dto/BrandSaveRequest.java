package com.solusi.erp.inventory.brand.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Intent-Based DTO for Saving (Create/Update) Brand.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BrandSaveRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{label.brand.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.brand.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.brand.name} {validation.size.suffix}")
    private String name;

    private String note;
}
