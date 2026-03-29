package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.security.role.domain.model.Role;

import java.util.Set;

@FunctionalInterface
public interface CreateRoleUseCase {
    Role execute(String name, String description, Set<Long> permissionIds);
}

