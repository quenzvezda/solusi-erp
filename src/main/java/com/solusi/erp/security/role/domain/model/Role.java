package com.solusi.erp.security.role.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

import java.util.HashSet;
import java.util.Set;

public class Role {
    private final AuditMetadata metadata;
    private String name;
    private String description;
    private Set<Long> permissionIds;

    public Role(AuditMetadata metadata, String name, String description, Set<Long> permissionIds) {
        this.metadata = metadata;
        this.name = name;
        this.description = description;
        this.permissionIds = permissionIds == null ? new HashSet<>() : new HashSet<>(permissionIds);
    }

    public static Role createNew(String name, String description, Set<Long> permissionIds) {
        return new Role(AuditMetadata.empty(), name, description, permissionIds);
    }

    public void update(String name, String description, Set<Long> permissionIds) {
        this.name = name;
        this.description = description;
        this.permissionIds = permissionIds == null ? new HashSet<>() : new HashSet<>(permissionIds);
    }

    public boolean isAdminRole() {
        return "ROLE_ADMIN".equals(name);
    }

    public Long getId() {
        return metadata.id();
    }

    public AuditMetadata getMetadata() {
        return metadata;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<Long> getPermissionIds() {
        return new HashSet<>(permissionIds);
    }
}

