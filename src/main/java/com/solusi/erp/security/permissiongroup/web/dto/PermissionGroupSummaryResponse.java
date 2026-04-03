package com.solusi.erp.security.permissiongroup.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PermissionGroupSummaryResponse {
    private Long id;
    private String code;
    private String nameId;
    private String nameEn;
    private String breadcrumbId;
    private String breadcrumbEn;
    private String urlPath;
    private String iconClass;
    /** Localized display name for UI dropdowns — set by web mapper based on request locale. */
    private String name;
}
