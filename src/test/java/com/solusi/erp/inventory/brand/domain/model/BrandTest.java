package com.solusi.erp.inventory.brand.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Brand Domain Model Tests")
class BrandTest {

    @Test
    @DisplayName("createNew sets code, name, and note with empty metadata (id == null)")
    void createNew_setsCodeNameAndNoteWithEmptyMetadata() {
        Brand brand = Brand.createNew("BR-001", "Acme", "A note");

        assertThat(brand.getCode()).isEqualTo("BR-001");
        assertThat(brand.getName()).isEqualTo("Acme");
        assertThat(brand.getNote()).isEqualTo("A note");
        assertThat(brand.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Brand brand = new Brand(metadata, "BR-001", "Acme", "A note");

        assertThat(brand.getId()).isEqualTo(1L);
        assertThat(brand.getCode()).isEqualTo("BR-001");
        assertThat(brand.getName()).isEqualTo("Acme");
        assertThat(brand.getNote()).isEqualTo("A note");
        assertThat(brand.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("update changes name and note; code remains unchanged")
    void update_changesNameAndNote() {
        Brand brand = Brand.createNew("BR-001", "Original Name", "Original Note");

        brand.update("New Name", "New Note");

        assertThat(brand.getName()).isEqualTo("New Name");
        assertThat(brand.getNote()).isEqualTo("New Note");
        assertThat(brand.getCode()).isEqualTo("BR-001");
    }
}
