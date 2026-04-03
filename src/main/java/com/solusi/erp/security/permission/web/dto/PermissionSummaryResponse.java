package com.solusi.erp.security.permission.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PermissionSummaryResponse extends BaseAuditResponse {
    private String name;
    private String description;
    private Long permissionGroupId;
    private String permissionGroupName;
}
