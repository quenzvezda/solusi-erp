package com.solusi.erp.security.permissiongroup.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;

public class FindPermissionGroupsUseCaseImpl implements FindPermissionGroupsUseCase {
    private final PermissionGroupRepository repository;

    public FindPermissionGroupsUseCaseImpl(PermissionGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<PermissionGroup> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
