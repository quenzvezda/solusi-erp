package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;

import java.util.Optional;

public class FindRoleByIdUseCaseImpl implements FindRoleByIdUseCase {
    private final RoleRepository repository;

    public FindRoleByIdUseCaseImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Role> execute(Long id) {
        return repository.findById(id);
    }
}

