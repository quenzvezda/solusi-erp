package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;

public class DeletePermissionGroupUseCaseImpl implements DeletePermissionGroupUseCase {
    private final PermissionGroupRepository repository;

    public DeletePermissionGroupUseCaseImpl(PermissionGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        repository.delete(id);
    }
}
