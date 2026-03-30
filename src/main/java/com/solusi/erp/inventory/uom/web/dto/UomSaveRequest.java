package com.solusi.erp.inventory.uom.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Intent-Based DTO for Saving (Create/Update) UnitOfMeasure.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UomSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.uom.code} {validation.notblank.suffix}")
    @Size(max = 20, message = "{label.uom.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.uom.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.uom.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.uom.type} {validation.notblank.suffix}")
    private UomType type;
}
