package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.security.user.domain.model.User;

@FunctionalInterface
public interface UpdateProfileUseCase {
    User execute(String username,
                 String fullName,
                 String email,
                 String phoneNumber,
                 String languageCode,
                 Integer defaultPageSize,
                 String theme,
                 String currentPassword,
                 String newPassword,
                 String confirmPassword);
}
