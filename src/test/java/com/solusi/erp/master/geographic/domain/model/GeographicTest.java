package com.solusi.erp.master.geographic.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.shared.model.GeographicType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Geographic Domain Model Tests")
class GeographicTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        Geographic geo = Geographic.createNew("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);

        assertThat(geo.getCode()).isEqualTo("ID");
        assertThat(geo.getName()).isEqualTo("Indonesia");
        assertThat(geo.getType()).isEqualTo(GeographicType.COUNTRY);
        assertThat(geo.getParentId()).isNull();
        assertThat(geo.getParentName()).isNull();
        assertThat(geo.getIsActive()).isTrue();
        assertThat(geo.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Geographic geo = new Geographic(metadata, "JKT", "Jakarta",
                GeographicType.CITY_MUNICIPALITY, 2L, "DKI Jakarta", Boolean.TRUE);

        assertThat(geo.getId()).isEqualTo(1L);
        assertThat(geo.getCode()).isEqualTo("JKT");
        assertThat(geo.getName()).isEqualTo("Jakarta");
        assertThat(geo.getType()).isEqualTo(GeographicType.CITY_MUNICIPALITY);
        assertThat(geo.getParentId()).isEqualTo(2L);
        assertThat(geo.getParentName()).isEqualTo("DKI Jakarta");
        assertThat(geo.getIsActive()).isTrue();
        assertThat(geo.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("update changes mutable fields; code remains unchanged")
    void update_changesMutableFields() {
        Geographic geo = Geographic.createNew("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);

        geo.update("Republic of Indonesia", GeographicType.COUNTRY, null, null, Boolean.FALSE);

        assertThat(geo.getName()).isEqualTo("Republic of Indonesia");
        assertThat(geo.getIsActive()).isFalse();
        assertThat(geo.getCode()).isEqualTo("ID");
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveToFalse() {
        Geographic geo = Geographic.createNew("ID", "Indonesia", GeographicType.COUNTRY,
                null, null, Boolean.TRUE);

        geo.softDelete();

        assertThat(geo.getIsActive()).isFalse();
    }
}

