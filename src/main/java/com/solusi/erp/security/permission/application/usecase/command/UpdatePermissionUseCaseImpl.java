package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;

public class UpdatePermissionUseCaseImpl implements UpdatePermissionUseCase {
    private final PermissionRepository repository;

    public UpdatePermissionUseCaseImpl(PermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Permission execute(Long id, String name, String description, Long permissionGroupId) {
        Permission permission = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.permission.notfound"));

        String normalizedName = normalizeName(name);
        repository.findByName(normalizedName)
                .ifPresent(existing -> {
                    if (existing.getId() == null || !existing.getId().equals(id)) {
                        throw new DomainException("msg.error.permission.exists", normalizedName);
                    }
                });

        permission.update(normalizedName, description, permissionGroupId);
        return repository.save(permission);
    }

    private String normalizeName(String rawName) {
        return rawName == null ? "" : rawName.trim().toUpperCase().replace(" ", "-");
    }
}
