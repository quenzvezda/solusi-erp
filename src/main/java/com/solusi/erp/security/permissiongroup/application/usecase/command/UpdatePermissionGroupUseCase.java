package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;

public interface UpdatePermissionGroupUseCase {
    PermissionGroup execute(Long id, String nameId, String nameEn,
                            String breadcrumbId, String breadcrumbEn, String urlPath,
                            String iconClass, String descriptionId, String descriptionEn);
}
