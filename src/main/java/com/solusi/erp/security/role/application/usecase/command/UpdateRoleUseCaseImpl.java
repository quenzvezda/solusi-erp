package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;

import java.util.Locale;
import java.util.Set;

public class UpdateRoleUseCaseImpl implements UpdateRoleUseCase {
    private final RoleRepository repository;

    public UpdateRoleUseCaseImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Role execute(Long id, String name, String description, Set<Long> permissionIds) {
        Role role = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.role.notfound"));

        String normalizedName = normalizeName(name);
        repository.findByName(normalizedName)
                .ifPresent(existing -> {
                    if (existing.getId() == null || !existing.getId().equals(id)) {
                        throw new DomainException("msg.error.role.duplicate");
                    }
                });

        role.update(normalizedName, description, permissionIds);
        return repository.save(role);
    }

    private String normalizeName(String rawName) {
        return rawName == null ? "" : rawName.trim().toUpperCase(Locale.ROOT);
    }
}

