package com.solusi.erp.security.permission.application.usecase.query;

import com.solusi.erp.security.permission.domain.model.Permission;

import java.util.Optional;

@FunctionalInterface
public interface FindPermissionByIdUseCase {
    Optional<Permission> execute(Long id);
}
