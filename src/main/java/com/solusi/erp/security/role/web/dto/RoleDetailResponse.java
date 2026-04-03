package com.solusi.erp.security.role.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import com.solusi.erp.security.permission.web.dto.PermissionSummaryResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoleDetailResponse extends BaseAuditResponse {
    private String name;
    private String description;
    private Set<PermissionSummaryResponse> permissions;
}

