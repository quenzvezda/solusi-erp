package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.security.role.domain.model.Role;

import java.util.Optional;

@FunctionalInterface
public interface FindRoleByIdUseCase {
    Optional<Role> execute(Long id);
}

