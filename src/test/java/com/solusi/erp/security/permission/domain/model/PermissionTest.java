package com.solusi.erp.security.permission.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionTest {

    @Test
    void createNew_setsFields() {
        Permission permission = Permission.createNew("INV_READ", "Read inventory", 10L);

        assertThat(permission.getId()).isNull();
        assertThat(permission.getName()).isEqualTo("INV_READ");
        assertThat(permission.getDescription()).isEqualTo("Read inventory");
        assertThat(permission.getPermissionGroupId()).isEqualTo(10L);
    }

    @Test
    void update_changesEditableFields() {
        Permission permission = Permission.createNew("INV_READ", "Read inventory", 10L);

        permission.update("INV_CREATE", "Create inventory", 20L);

        assertThat(permission.getName()).isEqualTo("INV_CREATE");
        assertThat(permission.getDescription()).isEqualTo("Create inventory");
        assertThat(permission.getPermissionGroupId()).isEqualTo(20L);
    }

    @Test
    void isSystemPermission_detectsReservedPrefix() {
        Permission system = new Permission(new AuditMetadata(1L, 1L, null, null, null, null), "USERS_READ", null, null);
        Permission regular = new Permission(new AuditMetadata(2L, 1L, null, null, null, null), "INV_READ", null, null);

        assertThat(system.isSystemPermission()).isTrue();
        assertThat(regular.isSystemPermission()).isFalse();
    }
}
