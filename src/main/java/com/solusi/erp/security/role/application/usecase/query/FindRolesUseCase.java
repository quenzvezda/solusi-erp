package com.solusi.erp.security.role.application.usecase.query;

import com.solusi.erp.security.role.domain.model.Role;

import java.util.List;

@FunctionalInterface
public interface FindRolesUseCase {
    List<Role> execute();
}

