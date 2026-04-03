package com.solusi.erp.security.permissiongroup.domain.model;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PermissionGroupTest {

    @Test
    void createNew_setsAllFields() {
        PermissionGroup pg = PermissionGroup.createNew(
                "SEC-01", "Keamanan", "Security",
                "Keamanan", "Security", "/security",
                "ti-shield", "Modul keamanan", "Security module");

        assertThat(pg.getId()).isNull();
        assertThat(pg.getCode()).isEqualTo("SEC-01");
        assertThat(pg.getNameId()).isEqualTo("Keamanan");
        assertThat(pg.getNameEn()).isEqualTo("Security");
        assertThat(pg.getUrlPath()).isEqualTo("/security");
    }

    @Test
    void update_changesAllEditableFields() {
        PermissionGroup pg = PermissionGroup.createNew(
                "SEC-01", "Keamanan", "Security",
                "Keamanan", "Security", "/security",
                null, null, null);

        pg.update("Keamanan2", "Security2", "Keamanan2", "Security2",
                "/security2", "ti-lock", "Desc ID", "Desc EN");

        assertThat(pg.getNameId()).isEqualTo("Keamanan2");
        assertThat(pg.getNameEn()).isEqualTo("Security2");
        assertThat(pg.getUrlPath()).isEqualTo("/security2");
        assertThat(pg.getIconClass()).isEqualTo("ti-lock");
    }

    @Test
    void createNew_codeIsImmutable() {
        PermissionGroup pg = PermissionGroup.createNew(
                "IMMUTABLE-CODE", "N", "N", "B", "B", "/u", null, null, null);
        assertThat(pg.getCode()).isEqualTo("IMMUTABLE-CODE");
    }
}
