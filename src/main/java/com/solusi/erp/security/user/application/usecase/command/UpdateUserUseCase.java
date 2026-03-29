package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.security.user.domain.model.User;

@FunctionalInterface
public interface UpdateUserUseCase {
    User execute(Long id,
                 String username,
                 String email,
                 String rawPassword,
                 Long roleId,
                 String fullName,
                 String phoneNumber,
                 Boolean enabled,
                 Boolean passwordChangeRequired,
                 Long partyId);
}
