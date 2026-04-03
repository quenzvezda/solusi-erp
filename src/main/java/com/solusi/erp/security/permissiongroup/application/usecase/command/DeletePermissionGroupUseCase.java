package com.solusi.erp.security.permissiongroup.application.usecase.command;

@FunctionalInterface
public interface DeletePermissionGroupUseCase {
    void execute(Long id);
}
