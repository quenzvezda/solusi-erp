package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;

import java.util.Optional;

public class FindUserByIdUseCaseImpl implements FindUserByIdUseCase {
    private final UserRepository repository;

    public FindUserByIdUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> execute(Long id) {
        return repository.findById(id);
    }
}
