package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.security.user.domain.model.User;

@FunctionalInterface
public interface ToggleUserStatusUseCase {
    User execute(Long id);
}
