package com.solusi.erp.security.service;

import com.solusi.erp.core.dto.FormViewDto;
import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.security.dto.PermissionResponse;
import com.solusi.erp.security.dto.RoleRequest;
import com.solusi.erp.security.dto.RoleResponse;

import java.util.List;

public interface RoleService {
    List<RoleResponse> findAll();
    RoleResponse findById(Long id);
    List<PermissionResponse> findAllPermissions();
    FormViewDto<RoleRequest, Void, RoleResponse> getRoleEditView(Long id);
    RoleResponse create(RoleRequest request);
    RoleResponse update(Long id, RoleRequest request);
    void delete(Long id);

    List<LookupDto> lookupRoles(String keyword, int limit);
    LookupDto getLookupRole(Long id);
}
