package com.solusi.erp.security.permissiongroup.domain.repository;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
import com.solusi.erp.security.permissiongroup.domain.model.PermissionGroup;

import java.util.Optional;

public interface PermissionGroupRepository {
    PermissionGroup save(PermissionGroup group);
    Optional<PermissionGroup> findById(Long id);
    Page<PermissionGroup> findAll(String keyword, Pageable pageable);
    void delete(Long id);
    boolean existsByCode(String code);
}
