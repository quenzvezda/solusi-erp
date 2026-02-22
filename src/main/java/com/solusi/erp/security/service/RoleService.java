package com.solusi.erp.security.service;

import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.dto.RoleRequest;
import com.solusi.erp.security.dto.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> findAll();
    RoleResponse findById(Long id);
    List<PermissionResponse> findAllPermissions();
    void create(RoleRequest request);
    void update(Long id, RoleRequest request);
    void delete(Long id);
}
