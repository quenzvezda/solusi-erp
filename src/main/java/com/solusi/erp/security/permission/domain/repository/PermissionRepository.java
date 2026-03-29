package com.solusi.erp.security.permission.domain.repository;

import com.solusi.erp.security.permission.domain.model.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository {
    Permission save(Permission permission);
    Optional<Permission> findById(Long id);
    Optional<Permission> findByName(String name);
    List<Permission> findAll();
    void delete(Permission permission);
    boolean existsByName(String name);
}
