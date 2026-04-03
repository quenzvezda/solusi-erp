package com.solusi.erp.security.permissiongroup.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;
import com.solusi.erp.security.permissiongroup.domain.repository.PermissionGroupRepository;

public class CreatePermissionGroupUseCaseImpl implements CreatePermissionGroupUseCase {
    private final PermissionGroupRepository repository;

    public CreatePermissionGroupUseCaseImpl(PermissionGroupRepository repository) {
        this.repository = repository;
    }

    @Override
    public PermissionGroup execute(String code, String nameId, String nameEn,
                                   String breadcrumbId, String breadcrumbEn, String urlPath,
                                   String iconClass, String descriptionId, String descriptionEn) {
        if (repository.existsByCode(code)) {
            throw new DomainException("msg.error.common.duplicate");
        }
        PermissionGroup group = PermissionGroup.createNew(code, nameId, nameEn,
                breadcrumbId, breadcrumbEn, urlPath, iconClass, descriptionId, descriptionEn);
        return repository.save(group);
    }
}
