package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.application.port.PasswordCipher;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.model.UserProfile;
import com.solusi.erp.security.user.domain.repository.UserRepository;

public class UpdateProfileUseCaseImpl implements UpdateProfileUseCase {
    private final UserRepository repository;
    private final PasswordCipher passwordCipher;

    public UpdateProfileUseCaseImpl(UserRepository repository, PasswordCipher passwordCipher) {
        this.repository = repository;
        this.passwordCipher = passwordCipher;
    }

    @Override
    public User execute(String username,
                        String fullName,
                        String email,
                        String phoneNumber,
                        String languageCode,
                        Integer defaultPageSize,
                        String theme,
                        String currentPassword,
                        String newPassword,
                        String confirmPassword) {
        User user = repository.findByUsername(username)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));

        repository.findByEmail(email).ifPresent(existing -> {
            if (!existing.getId().equals(user.getId())) {
                throw new DomainException("msg.error.user.email-exists");
            }
        });

        UserProfile profile = user.getProfile() == null
                ? UserProfile.createDefault(fullName, phoneNumber)
                : user.getProfile();

        profile.update(fullName, phoneNumber, languageCode, defaultPageSize, theme);
        user.updateFromForm(user.getUsername(), email, user.getRoleId(), fullName, phoneNumber, user.isEnabled(), user.isPasswordChangeRequired());

        if (newPassword != null && !newPassword.isBlank()) {
            if (currentPassword == null || currentPassword.isBlank()) {
                throw new DomainException("msg.error.user.current-password-required");
            }
            if (!passwordCipher.matches(currentPassword, user.getPassword())) {
                throw new DomainException("msg.error.user.current-password-mismatch");
            }
            if (!newPassword.equals(confirmPassword)) {
                throw new DomainException("msg.error.user.confirm-password-mismatch");
            }
            if (newPassword.length() < 6) {
                throw new DomainException("msg.error.user.password-too-short");
            }
            user.updatePassword(passwordCipher.encode(newPassword), false);
        }

        return repository.save(user);
    }
}
