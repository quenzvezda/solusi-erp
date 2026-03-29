package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.security.user.domain.model.User;

@FunctionalInterface
public interface CreateUserUseCase {
    User execute(String username,
                 String email,
                 String rawPassword,
                 Long roleId,
                 String fullName,
                 String phoneNumber,
                 Boolean enabled,
                 Long partyId);
}
