package com.solusi.erp.security.service;

import com.solusi.erp.security.dto.PermissionRequest;
import com.solusi.erp.security.dto.PermissionResponse;

import java.util.List;

public interface PermissionService {
    List<PermissionResponse> findAll();
    void create(PermissionRequest request);
    void createBatch(PermissionRequest request);
    void delete(Long id);
}
