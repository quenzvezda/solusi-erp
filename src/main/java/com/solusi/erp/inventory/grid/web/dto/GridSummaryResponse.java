package com.solusi.erp.inventory.grid.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GridSummaryResponse extends BaseAuditResponse {
    private Long facilityId;
    private String facilityName;
    private String code;
    private String name;
    private Boolean isActive;
}
