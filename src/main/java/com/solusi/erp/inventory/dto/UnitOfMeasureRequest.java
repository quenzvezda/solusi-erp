package com.solusi.erp.inventory.dto;

import com.solusi.erp.inventory.model.UomType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating or updating a Unit of Measure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureRequest {

    private Long id;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 20, message = "{validation.size}")
    private String code;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 100, message = "{validation.size}")
    private String name;

    @NotNull(message = "{validation.notnull}")
    private UomType type;
}
