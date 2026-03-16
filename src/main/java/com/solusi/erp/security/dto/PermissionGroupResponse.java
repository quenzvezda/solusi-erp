package com.solusi.erp.security.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PermissionGroupResponse extends BaseAuditResponse {
    private String code;
    private String nameId;
    private String nameEn;
    private String breadcrumbId;
    private String breadcrumbEn;
    private String urlPath;
    private String iconClass;
    private String descriptionId;
    private String descriptionEn;

    // Helper for UI
    private String name;
}
