package com.solusi.erp.security.service;

import com.solusi.erp.security.dto.PermissionGroupRequest;
import com.solusi.erp.security.dto.PermissionGroupResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PermissionGroupService {
    List<PermissionGroupResponse> findAll();
    Page<PermissionGroupResponse> findAll(String keyword, Pageable pageable);
    PermissionGroupRequest getById(Long id);
    void create(PermissionGroupRequest request);
    void update(Long id, PermissionGroupRequest request);
    void delete(Long id);
    List<com.solusi.erp.security.dto.MenuNodeResponse> buildMenuTree(java.util.Collection<String> authorities);
}
