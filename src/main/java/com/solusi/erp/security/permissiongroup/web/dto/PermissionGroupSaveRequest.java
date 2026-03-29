package com.solusi.erp.security.permissiongroup.web.dto;

import com.solusi.erp.core.dto.BaseAuditResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PermissionGroupSaveRequest extends BaseAuditResponse {

    @NotBlank(message = "{label.menu.group.code} {validation.notblank.suffix}")
    @Size(max = 50, message = "{label.menu.group.code} {validation.size.suffix}")
    private String code;

    @NotBlank(message = "{label.menu.group.name.id} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.menu.group.name.id} {validation.size.suffix}")
    private String nameId;

    @NotBlank(message = "{label.menu.group.name.en} {validation.notblank.suffix}")
    @Size(max = 100, message = "{label.menu.group.name.en} {validation.size.suffix}")
    private String nameEn;

    @NotBlank(message = "{label.menu.group.breadcrumb.id} {validation.notblank.suffix}")
    @Size(max = 255, message = "{label.menu.group.breadcrumb.id} {validation.size.suffix}")
    private String breadcrumbId;

    @NotBlank(message = "{label.menu.group.breadcrumb.en} {validation.notblank.suffix}")
    @Size(max = 255, message = "{label.menu.group.breadcrumb.en} {validation.size.suffix}")
    private String breadcrumbEn;

    @NotBlank(message = "{label.menu.group.url} {validation.notblank.suffix}")
    @Size(max = 255, message = "{label.menu.group.url} {validation.size.suffix}")
    private String urlPath;

    @Size(max = 50, message = "Icon {validation.size.suffix}")
    private String iconClass;

    @Size(max = 255, message = "{label.menu.group.description.id} {validation.size.suffix}")
    private String descriptionId;

    @Size(max = 255, message = "{label.menu.group.description.en} {validation.size.suffix}")
    private String descriptionEn;
}
