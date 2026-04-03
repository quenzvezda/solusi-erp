package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;

public class UpdatePermissionGroupUseCaseImpl implements UpdatePermissionGroupUseCase {
    private final PermissionGroupRepository repository;

    public UpdatePermissionGroupUseCaseImpl(PermissionGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public PermissionGroup execute(Long id, String nameId, String nameEn,
                                   String breadcrumbId, String breadcrumbEn, String urlPath,
                                   String iconClass, String descriptionId, String descriptionEn) {
        PermissionGroup group = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PermissionGroup not found: " + id));
        group.update(nameId, nameEn, breadcrumbId, breadcrumbEn, urlPath, iconClass, descriptionId, descriptionEn);
        return repository.save(group);
    }
}
