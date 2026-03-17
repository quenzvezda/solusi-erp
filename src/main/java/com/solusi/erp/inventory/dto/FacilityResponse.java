package com.solusi.erp.inventory.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Facility.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FacilityResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private Long ownerId;
    private String ownerName;
    private String addressLine1;
    private Long cityId;
    private String cityName;
    private String postalCode;
    private String note;
    private Boolean isActive;
}
