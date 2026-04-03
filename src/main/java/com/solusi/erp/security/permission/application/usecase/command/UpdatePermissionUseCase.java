package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.security.permission.domain.model.Permission;

@FunctionalInterface
public interface UpdatePermissionUseCase {
    Permission execute(Long id, String name, String description, Long permissionGroupId);
}
