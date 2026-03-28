package com.solusi.erp.master.partyroletype.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PartyRoleType Domain Model Tests")
class PartyRoleTypeTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        PartyRoleType prt = PartyRoleType.createNew("PRT-001", "Customer", "Note", true);

        assertThat(prt.getCode()).isEqualTo("PRT-001");
        assertThat(prt.getName()).isEqualTo("Customer");
        assertThat(prt.getNote()).isEqualTo("Note");
        assertThat(prt.getIsActive()).isTrue();
        assertThat(prt.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        PartyRoleType prt = new PartyRoleType(metadata, "PRT-001", "Customer", "Note", true);

        assertThat(prt.getId()).isEqualTo(1L);
        assertThat(prt.getCode()).isEqualTo("PRT-001");
        assertThat(prt.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("update changes mutable fields; code remains unchanged")
    void update_changesMutableFields() {
        PartyRoleType prt = PartyRoleType.createNew("PRT-001", "Old Name", "Old Note", true);

        prt.update("New Name", "New Note", false);

        assertThat(prt.getName()).isEqualTo("New Name");
        assertThat(prt.getNote()).isEqualTo("New Note");
        assertThat(prt.getIsActive()).isFalse();
        assertThat(prt.getCode()).isEqualTo("PRT-001");
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveToFalse() {
        PartyRoleType prt = PartyRoleType.createNew("PRT-001", "Customer", null, true);
        prt.softDelete();
        assertThat(prt.getIsActive()).isFalse();
    }
}
