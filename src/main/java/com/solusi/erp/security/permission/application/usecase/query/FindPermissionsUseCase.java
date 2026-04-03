package com.solusi.erp.security.permission.application.usecase.query;

import com.solusi.erp.security.permission.domain.model.Permission;

import java.util.List;

@FunctionalInterface
public interface FindPermissionsUseCase {
    List<Permission> execute();
}
