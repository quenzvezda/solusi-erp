package com.solusi.erp.inventory.uom.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.inventory.uom.domain.model.UomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UnitOfMeasure Domain Model Tests")
class UomDomainTest {

    @Test
    @DisplayName("createNew sets code, name, and type with empty metadata (id == null)")
    void createNew_setsCodeNameAndTypeWithEmptyMetadata() {
        UnitOfMeasure uom = UnitOfMeasure.createNew("KG", "Kilogram", UomType.WEIGHT);

        assertThat(uom.getCode()).isEqualTo("KG");
        assertThat(uom.getName()).isEqualTo("Kilogram");
        assertThat(uom.getType()).isEqualTo(UomType.WEIGHT);
        assertThat(uom.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        UnitOfMeasure uom = new UnitOfMeasure(metadata, "KG", "Kilogram", UomType.WEIGHT);

        assertThat(uom.getId()).isEqualTo(1L);
        assertThat(uom.getCode()).isEqualTo("KG");
        assertThat(uom.getName()).isEqualTo("Kilogram");
        assertThat(uom.getType()).isEqualTo(UomType.WEIGHT);
        assertThat(uom.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("update changes name and type; code remains unchanged")
    void update_changesNameAndType() {
        UnitOfMeasure uom = UnitOfMeasure.createNew("KG", "Kilogram", UomType.WEIGHT);

        uom.update("Liter", UomType.VOLUME);

        assertThat(uom.getName()).isEqualTo("Liter");
        assertThat(uom.getType()).isEqualTo(UomType.VOLUME);
        assertThat(uom.getCode()).isEqualTo("KG");
    }
}
