package com.solusi.erp.inventory.facility.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FacilitySummaryResponse extends BaseAuditResponse {
    private String code;
    private String name;
    private Long ownerId;
    private String ownerName;
    private String addressLine1;
    private Long cityId;
    private String cityName;
    private String postalCode;
    private Boolean isActive;
}
