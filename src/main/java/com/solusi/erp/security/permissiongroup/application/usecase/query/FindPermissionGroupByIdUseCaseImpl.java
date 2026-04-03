package com.solusi.erp.security.permissiongroup.application.usecase.query;

import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;

import java.util.Optional;

public class FindPermissionGroupByIdUseCaseImpl implements FindPermissionGroupByIdUseCase {
    private final PermissionGroupRepository repository;

    public FindPermissionGroupByIdUseCaseImpl(PermissionGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PermissionGroup> execute(Long id) {
        return repository.findById(id);
    }
}
