package com.solusi.erp.security.permissiongroup.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;

@FunctionalInterface
public interface FindPermissionGroupsUseCase {
    Page<PermissionGroup> execute(String keyword, Pageable pageable);
}
