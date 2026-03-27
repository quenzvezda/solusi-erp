package com.solusi.erp.inventory.container.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Container Domain Model Tests")
class ContainerDomainTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata and null code")
    void createNew_setsFieldsWithNullCode() {
        Container container = Container.createNew(
            1L, "Bin A1", "BARC-001",
            BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30),
            BigDecimal.valueOf(100), "Note", true
        );

        assertThat(container.getGridId()).isEqualTo(1L);
        assertThat(container.getName()).isEqualTo("Bin A1");
        assertThat(container.getBarcode()).isEqualTo("BARC-001");
        assertThat(container.getLength()).isEqualByComparingTo(BigDecimal.valueOf(10));
        assertThat(container.getWidth()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(container.getHeight()).isEqualByComparingTo(BigDecimal.valueOf(30));
        assertThat(container.getMaxWeight()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(container.getNote()).isEqualTo("Note");
        assertThat(container.getIsActive()).isTrue();
        assertThat(container.getCode()).isNull();
        assertThat(container.getId()).isNull();
    }

    @Test
    @DisplayName("assignCode sets the code")
    void assignCode_setsCode() {
        Container container = Container.createNew(1L, "Bin A1", null, null, null, null, null, null, true);
        container.assignCode("CNT-001");
        assertThat(container.getCode()).isEqualTo("CNT-001");
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(3L, 1L, null, null, null, null);
        Container container = new Container(metadata, 1L, "Storage A", "Main WH", "CNT-001",
            "Bin A1", "BARC-001",
            BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(30),
            BigDecimal.valueOf(100), "Note", true);

        assertThat(container.getId()).isEqualTo(3L);
        assertThat(container.getGridName()).isEqualTo("Storage A");
        assertThat(container.getFacilityName()).isEqualTo("Main WH");
        assertThat(container.getCode()).isEqualTo("CNT-001");
    }

    @Test
    @DisplayName("update changes mutable fields; code and gridId remain unchanged")
    void update_changesMutableFields() {
        Container container = Container.createNew(1L, "Old Name", "OLD-BARC", null, null, null, null, "Old Note", true);
        container.assignCode("CNT-001");

        container.update("New Name", "NEW-BARC",
            BigDecimal.valueOf(5), BigDecimal.valueOf(6), BigDecimal.valueOf(7),
            BigDecimal.valueOf(50), "New Note", false);

        assertThat(container.getName()).isEqualTo("New Name");
        assertThat(container.getBarcode()).isEqualTo("NEW-BARC");
        assertThat(container.getLength()).isEqualByComparingTo(BigDecimal.valueOf(5));
        assertThat(container.getIsActive()).isFalse();
        assertThat(container.getCode()).isEqualTo("CNT-001"); // unchanged
        assertThat(container.getGridId()).isEqualTo(1L); // unchanged
    }
}
