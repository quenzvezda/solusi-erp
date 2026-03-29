package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.security.permission.domain.model.Permission;

@FunctionalInterface
public interface CreatePermissionUseCase {
    Permission execute(String name, String description, Long permissionGroupId);
}
