package com.solusi.erp.security.permissiongroup.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PermissionGroupDetailResponse extends BaseAuditResponse {
    private String code;
    private String nameId;
    private String nameEn;
    private String breadcrumbId;
    private String breadcrumbEn;
    private String urlPath;
    private String iconClass;
    private String descriptionId;
    private String descriptionEn;
}
