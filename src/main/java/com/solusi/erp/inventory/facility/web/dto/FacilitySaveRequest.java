package com.solusi.erp.inventory.facility.web.dto;

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
public class FacilitySaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.facility.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.facility.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.facility.owner} {validation.notblank.suffix}")
    private Long ownerId;

    private String addressLine1;

    private Long cityId;

    private String postalCode;

    private String note;

    private Boolean isActive = Boolean.TRUE;
}
