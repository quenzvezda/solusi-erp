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
 * Request DTO for Facility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FacilityRequest extends BaseAuditResponse {

    @Size(max = 50, message = "{label.facility.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.facility.name} {validation.notblank.suffix}")
    @Size(max = 150, message = "{label.facility.name} {validation.size.suffix}")
    private String name;

    @NotNull(message = "{label.facility.owner} {validation.notnull.suffix}")
    private Long ownerId;

    @NotBlank(message = "{label.party.address} {validation.notblank.suffix}")
    private String addressLine1;

    @NotNull(message = "{label.geographic.city} {validation.notnull.suffix}")
    private Long cityId;

    @NotBlank(message = "{label.geographic.postal_code} {validation.notblank.suffix}")
    @Size(max = 20, message = "{label.geographic.postal_code} {validation.size.suffix}")
    private String postalCode;

    private String note;

    @Builder.Default
    private Boolean isActive = true;
}
