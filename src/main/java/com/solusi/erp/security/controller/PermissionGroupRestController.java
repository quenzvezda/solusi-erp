package com.solusi.erp.security.controller;

import com.solusi.erp.security.dto.PermissionGroupRequest;
import com.solusi.erp.security.dto.PermissionGroupResponse;
import com.solusi.erp.security.service.PermissionGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/security/permission-groups")
@RequiredArgsConstructor
public class PermissionGroupRestController {

    private final PermissionGroupService permissionGroupService;

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISSIONS_READ')")
    public List<PermissionGroupResponse> findAll() {
        return permissionGroupService.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PERMISSIONS_CREATE')")
    public PermissionGroupResponse create(@Valid @RequestBody PermissionGroupRequest request) {
        permissionGroupService.create(request);
        return PermissionGroupResponse.builder().name("Success").build();
    }
}
