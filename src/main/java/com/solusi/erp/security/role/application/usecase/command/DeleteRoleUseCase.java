package com.solusi.erp.security.role.application.usecase.command;

@FunctionalInterface
public interface DeleteRoleUseCase {
    void execute(Long id);
}

