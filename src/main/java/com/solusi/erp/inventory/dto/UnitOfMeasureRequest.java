package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.inventory.model.UomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Unit of Measure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UnitOfMeasureRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.uom.code} {validation.notblank.suffix}")
    @Size(max = 20, message = "{label.uom.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.uom.name} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.uom.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.uom.type} {validation.notnull.suffix}")
    private UomType type;
}
