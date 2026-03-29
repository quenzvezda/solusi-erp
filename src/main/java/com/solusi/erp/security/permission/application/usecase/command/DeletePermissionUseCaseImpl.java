package com.solusi.erp.security.permission.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;

public class DeletePermissionUseCaseImpl implements DeletePermissionUseCase {
    private final PermissionRepository repository;

    public DeletePermissionUseCaseImpl(PermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        Permission permission = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.permission.notfound"));

        if (permission.isSystemPermission()) {
            throw new DomainException("msg.error.permission.system.nodelete");
        }

        repository.delete(permission);
    }
}
