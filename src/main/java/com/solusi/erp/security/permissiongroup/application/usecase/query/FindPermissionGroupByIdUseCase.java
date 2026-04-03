package com.solusi.erp.security.permissiongroup.application.usecase.query;

import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;

import java.util.Optional;

@FunctionalInterface
public interface FindPermissionGroupByIdUseCase {
    Optional<PermissionGroup> execute(Long id);
}
