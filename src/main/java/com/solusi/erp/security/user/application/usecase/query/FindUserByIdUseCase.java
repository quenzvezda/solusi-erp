package com.solusi.erp.security.user.application.usecase.query;

import com.solusi.erp.security.user.domain.model.User;

import java.util.Optional;

@FunctionalInterface
public interface FindUserByIdUseCase {
    Optional<User> execute(Long id);
}
