package com.solusi.erp.inventory.facility.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Facility Domain Model Tests")
class FacilityDomainTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata and null code")
    void createNew_setsFieldsWithNullCode() {
        Facility facility = Facility.createNew("Warehouse A", 1L, "Jl. Test 1", 10L, "12345", "A note", true);

        assertThat(facility.getName()).isEqualTo("Warehouse A");
        assertThat(facility.getOwnerId()).isEqualTo(1L);
        assertThat(facility.getAddressLine1()).isEqualTo("Jl. Test 1");
        assertThat(facility.getCityId()).isEqualTo(10L);
        assertThat(facility.getPostalCode()).isEqualTo("12345");
        assertThat(facility.getNote()).isEqualTo("A note");
        assertThat(facility.getIsActive()).isTrue();
        assertThat(facility.getCode()).isNull();
        assertThat(facility.getId()).isNull();
    }

    @Test
    @DisplayName("assignCode sets the code")
    void assignCode_setsCode() {
        Facility facility = Facility.createNew("Warehouse A", 1L, "Jl. Test 1", 10L, "12345", null, true);
        facility.assignCode("FAC-001");
        assertThat(facility.getCode()).isEqualTo("FAC-001");
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(5L, 1L, null, null, null, null);
        Facility facility = new Facility(metadata, "FAC-001", "Warehouse A", 1L, "Owner A",
            "Jl. Test 1", 10L, "Jakarta", "12345", "Note", true);

        assertThat(facility.getId()).isEqualTo(5L);
        assertThat(facility.getCode()).isEqualTo("FAC-001");
        assertThat(facility.getName()).isEqualTo("Warehouse A");
        assertThat(facility.getOwnerName()).isEqualTo("Owner A");
        assertThat(facility.getCityName()).isEqualTo("Jakarta");
    }

    @Test
    @DisplayName("update changes mutable fields; code remains unchanged")
    void update_changesMutableFields() {
        Facility facility = Facility.createNew("Old Name", 1L, "Old Addr", 10L, "11111", "Old Note", true);
        facility.assignCode("FAC-001");

        facility.update("New Name", 2L, "New Addr", 20L, "22222", "New Note", false);

        assertThat(facility.getName()).isEqualTo("New Name");
        assertThat(facility.getOwnerId()).isEqualTo(2L);
        assertThat(facility.getAddressLine1()).isEqualTo("New Addr");
        assertThat(facility.getCityId()).isEqualTo(20L);
        assertThat(facility.getPostalCode()).isEqualTo("22222");
        assertThat(facility.getNote()).isEqualTo("New Note");
        assertThat(facility.getIsActive()).isFalse();
        assertThat(facility.getCode()).isEqualTo("FAC-001"); // unchanged
    }
}
