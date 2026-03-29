package com.solusi.erp.inventory.grid.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GridSaveRequest extends BaseAuditResponse {

    @NotNull(message = "{label.grid.facility} {validation.notblank.suffix}")
    private Long facilityId;

    private String facilityName;

    @NotBlank(message = "{label.grid.code} {validation.notblank.suffix}")
    @Size(max = 50, message = "{label.grid.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.grid.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.grid.name} {validation.size.suffix}")
    private String name;

    private String note;

    private Boolean isActive = Boolean.TRUE;
}
