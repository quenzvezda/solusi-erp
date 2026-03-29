package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;

import java.util.Locale;
import java.util.Set;

public class CreateRoleUseCaseImpl implements CreateRoleUseCase {
    private final RoleRepository repository;

    public CreateRoleUseCaseImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Role execute(String name, String description, Set<Long> permissionIds) {
        String normalizedName = normalizeName(name);
        if (repository.existsByName(normalizedName)) {
            throw new DomainException("msg.error.role.duplicate");
        }
        Role role = Role.createNew(normalizedName, description, permissionIds);
        return repository.save(role);
    }

    private String normalizeName(String rawName) {
        return rawName == null ? "" : rawName.trim().toUpperCase(Locale.ROOT);
    }
}

