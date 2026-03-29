package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;

public class ToggleUserStatusUseCaseImpl implements ToggleUserStatusUseCase {
    private final UserRepository repository;

    public ToggleUserStatusUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        if (user.isAdminUsername()) {
            throw new DomainException("msg.error.user.toggle-admin");
        }
        user.toggleEnabled();
        return repository.save(user);
    }
}
