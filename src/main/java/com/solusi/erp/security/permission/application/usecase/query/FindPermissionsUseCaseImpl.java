package com.solusi.erp.security.permission.application.usecase.query;

import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;

import java.util.List;

public class FindPermissionsUseCaseImpl implements FindPermissionsUseCase {
    private final PermissionRepository repository;

    public FindPermissionsUseCaseImpl(PermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Permission> execute() {
        return repository.findAll();
    }
}
