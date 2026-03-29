package com.solusi.erp.security.role.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void createNew_setsFields() {
        Role role = Role.createNew("ROLE_STAFF", "Staff", Set.of(1L, 2L));

        assertThat(role.getId()).isNull();
        assertThat(role.getName()).isEqualTo("ROLE_STAFF");
        assertThat(role.getDescription()).isEqualTo("Staff");
        assertThat(role.getPermissionIds()).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void update_changesEditableFields() {
        Role role = Role.createNew("ROLE_STAFF", "Staff", Set.of(1L));

        role.update("ROLE_MANAGER", "Manager", Set.of(3L, 4L));

        assertThat(role.getName()).isEqualTo("ROLE_MANAGER");
        assertThat(role.getDescription()).isEqualTo("Manager");
        assertThat(role.getPermissionIds()).containsExactlyInAnyOrder(3L, 4L);
    }

    @Test
    void isAdminRole_detectsAdminName() {
        Role admin = new Role(new AuditMetadata(1L, 1L, null, null, null, null), "ROLE_ADMIN", null, Set.of());
        Role regular = new Role(new AuditMetadata(2L, 1L, null, null, null, null), "ROLE_USER", null, Set.of());

        assertThat(admin.isAdminRole()).isTrue();
        assertThat(regular.isAdminRole()).isFalse();
    }
}

