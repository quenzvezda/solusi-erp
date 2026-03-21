package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Grid.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GridRequest extends BaseAuditResponse {

    @NotNull(message = "{label.facility} {validation.notnull.suffix}")
    private Long facilityId;

    @NotBlank(message = "{label.grid.code} {validation.notblank.suffix}")
    @Size(max = 50, message = "{label.grid.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.grid.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.grid.name} {validation.size.suffix}")
    private String name;

    private String note;

    @Builder.Default
    private Boolean isActive = true;
}
