package com.solusi.erp.security.user.application.usecase.command;

@FunctionalInterface
public interface DeleteUserUseCase {
    void execute(Long id);
}
