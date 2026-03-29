package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;

import java.util.Optional;

public class GetProfileUpdateDataUseCaseImpl implements GetProfileUpdateDataUseCase {
    private final UserRepository repository;

    public GetProfileUpdateDataUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<User> execute(String username) {
        return repository.findByUsername(username);
    }
}
