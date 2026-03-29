package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;

import java.util.Optional;

public class GetProfileUseCaseImpl implements GetProfileUseCase {
    private final UserRepository repository;

    public GetProfileUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> execute(String username) {
        return repository.findByUsername(username);
    }
}
