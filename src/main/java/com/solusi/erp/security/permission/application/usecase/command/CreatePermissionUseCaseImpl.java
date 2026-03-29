package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;

public class CreatePermissionUseCaseImpl implements CreatePermissionUseCase {
    private final PermissionRepository repository;

    public CreatePermissionUseCaseImpl(PermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Permission execute(String name, String description, Long permissionGroupId) {
        String normalizedName = normalizeName(name);
        if (repository.existsByName(normalizedName)) {
            throw new DomainException("msg.error.permission.exists", normalizedName);
        }
        Permission permission = Permission.createNew(normalizedName, description, permissionGroupId);
        return repository.save(permission);
    }

    private String normalizeName(String rawName) {
        return rawName == null ? "" : rawName.trim().toUpperCase().replace(" ", "-");
    }
}
