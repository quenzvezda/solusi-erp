package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;

public interface CreatePermissionGroupUseCase {
    PermissionGroup execute(String code, String nameId, String nameEn,
                            String breadcrumbId, String breadcrumbEn, String urlPath,
                            String iconClass, String descriptionId, String descriptionEn);
}
