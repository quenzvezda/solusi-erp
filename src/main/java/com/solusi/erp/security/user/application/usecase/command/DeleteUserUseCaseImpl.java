package com.solusi.erp.security.user.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;

public class DeleteUserUseCaseImpl implements DeleteUserUseCase {
    private final UserRepository repository;

    public DeleteUserUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.user.notfound"));
        if (user.isAdminUsername()) {
            throw new DomainException("msg.error.user.delete-admin");
        }
        repository.deleteById(user.getId());
    }
}
