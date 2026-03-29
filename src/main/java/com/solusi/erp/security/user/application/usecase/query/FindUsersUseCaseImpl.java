package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.user.domain.model.User;
import com.solusi.erp.security.user.domain.repository.UserRepository;

public class FindUsersUseCaseImpl implements FindUsersUseCase {
    private final UserRepository repository;

    public FindUsersUseCaseImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<User> execute(String keyword, Pageable pageable) {
        return repository.findAll(keyword, pageable);
    }
}
