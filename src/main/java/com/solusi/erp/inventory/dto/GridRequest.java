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

    @NotNull(message = "{validation.notnull}")
    private Long facilityId;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 50, message = "{validation.size}")
    private String code;

    @NotBlank(message = "{validation.notblank}")
    @Size(max = 150, message = "{validation.size}")
    private String name;

    private String note;

    @Builder.Default
    private Boolean isActive = true;
}
