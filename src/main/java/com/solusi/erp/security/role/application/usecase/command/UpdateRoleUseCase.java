package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.security.role.domain.model.Role;

import java.util.Set;

@FunctionalInterface
public interface UpdateRoleUseCase {
    Role execute(Long id, String name, String description, Set<Long> permissionIds);
}

