package com.solusi.erp.security.role.application.usecase.command;

import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.security.role.domain.model.Role;
import com.solusi.erp.security.role.domain.repository.RoleRepository;

public class DeleteRoleUseCaseImpl implements DeleteRoleUseCase {
    private final RoleRepository repository;

    public DeleteRoleUseCaseImpl(RoleRepository repository) {
        this.repository = repository;
    }

    @Override
    public void execute(Long id) {
        Role role = repository.findById(id)
                .orElseThrow(() -> new DomainException("msg.error.role.notfound"));

        if (role.isAdminRole()) {
            throw new DomainException("msg.error.role.admin.nodelete");
        }

        repository.delete(role);
    }
}

