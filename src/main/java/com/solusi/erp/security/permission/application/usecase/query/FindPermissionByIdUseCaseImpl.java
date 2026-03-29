package com.solusi.erp.security.permission.application.usecase.query;

import com.solusi.erp.security.permission.domain.model.Permission;
import com.solusi.erp.security.permission.domain.repository.PermissionRepository;

import java.util.Optional;

public class FindPermissionByIdUseCaseImpl implements FindPermissionByIdUseCase {
    private final PermissionRepository repository;

    public FindPermissionByIdUseCaseImpl(PermissionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Permission> execute(Long id) {
        return repository.findById(id);
    }
}
