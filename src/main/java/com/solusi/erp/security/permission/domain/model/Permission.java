package com.solusi.erp.security.permission.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;

public class Permission {
    private final AuditMetadata metadata;
    private String name;
    private String description;
    private Long permissionGroupId;

    public Permission(AuditMetadata metadata, String name, String description, Long permissionGroupId) {
        this.metadata = metadata;
        this.name = name;
        this.description = description;
        this.permissionGroupId = permissionGroupId;
    }

    public static Permission createNew(String name, String description, Long permissionGroupId) {
        return new Permission(AuditMetadata.empty(), name, description, permissionGroupId);
    }

    public void update(String name, String description, Long permissionGroupId) {
        this.name = name;
        this.description = description;
        this.permissionGroupId = permissionGroupId;
    }

    public boolean isSystemPermission() {
        return name != null && (name.startsWith("USERS_")
                || name.startsWith("ROLES_")
                || name.startsWith("DASHBOARD_")
                || name.startsWith("PERMISSIONS_"));
    }

    public Long getId() { return metadata.id(); }
    public AuditMetadata getMetadata() { return metadata; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Long getPermissionGroupId() { return permissionGroupId; }
}
