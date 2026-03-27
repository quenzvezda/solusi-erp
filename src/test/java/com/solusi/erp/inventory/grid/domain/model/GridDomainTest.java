package com.solusi.erp.inventory.grid.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Grid Domain Model Tests")
class GridDomainTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata and null facilityName")
    void createNew_setsFieldsWithNullFacilityName() {
        Grid grid = Grid.createNew(1L, "GRD-001", "Storage Area A", "Note", true);

        assertThat(grid.getFacilityId()).isEqualTo(1L);
        assertThat(grid.getCode()).isEqualTo("GRD-001");
        assertThat(grid.getName()).isEqualTo("Storage Area A");
        assertThat(grid.getNote()).isEqualTo("Note");
        assertThat(grid.getIsActive()).isTrue();
        assertThat(grid.getFacilityName()).isNull();
        assertThat(grid.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(7L, 1L, null, null, null, null);
        Grid grid = new Grid(metadata, 1L, "Main Warehouse", "GRD-001", "Storage Area A", "Note", true);

        assertThat(grid.getId()).isEqualTo(7L);
        assertThat(grid.getFacilityName()).isEqualTo("Main Warehouse");
        assertThat(grid.getCode()).isEqualTo("GRD-001");
    }

    @Test
    @DisplayName("update changes name, note, isActive; code and facilityId unchanged")
    void update_changesMutableFields() {
        Grid grid = Grid.createNew(1L, "GRD-001", "Old Name", "Old Note", true);

        grid.update("New Name", "New Note", false);

        assertThat(grid.getName()).isEqualTo("New Name");
        assertThat(grid.getNote()).isEqualTo("New Note");
        assertThat(grid.getIsActive()).isFalse();
        assertThat(grid.getCode()).isEqualTo("GRD-001");
        assertThat(grid.getFacilityId()).isEqualTo(1L);
    }
}
