package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;

import java.util.List;

public class FindRolesUseCaseImpl implements FindRolesUseCase {
    private final RoleRepository repository;

    public FindRolesUseCaseImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Role> execute() {
        return repository.findAll();
    }
}

