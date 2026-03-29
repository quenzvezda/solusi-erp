package com.solusi.erp.security.permission.application.usecase.command;

@FunctionalInterface
public interface DeletePermissionUseCase {
    void execute(Long id);
}
