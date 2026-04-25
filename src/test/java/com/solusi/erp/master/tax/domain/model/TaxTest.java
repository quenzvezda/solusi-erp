package com.solusi.erp.master.tax.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tax Domain Model Tests")
class TaxTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        Tax tax = Tax.createNew("TX-01", "PPN", BigDecimal.valueOf(11), "Note", false, true);

        assertThat(tax.getCode()).isEqualTo("TX-01");
        assertThat(tax.getName()).isEqualTo("PPN");
        assertThat(tax.getRate()).isEqualByComparingTo(BigDecimal.valueOf(11));
        assertThat(tax.getNote()).isEqualTo("Note");
        assertThat(tax.getIsSubtract()).isFalse();
        assertThat(tax.getIsActive()).isTrue();
        assertThat(tax.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Tax tax = new Tax(metadata, "TX-01", "PPN", BigDecimal.valueOf(11), "Note", false, true);

        assertThat(tax.getId()).isEqualTo(1L);
        assertThat(tax.getCode()).isEqualTo("TX-01");
        assertThat(tax.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("update changes mutable fields; code remains unchanged")
    void update_changesMutableFields() {
        Tax tax = Tax.createNew("TX-01", "Old Name", BigDecimal.ONE, "Old Note", false, true);

        tax.update("New Name", BigDecimal.TEN, "New Note", true, false);

        assertThat(tax.getName()).isEqualTo("New Name");
        assertThat(tax.getRate()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(tax.getNote()).isEqualTo("New Note");
        assertThat(tax.getIsSubtract()).isTrue();
        assertThat(tax.getIsActive()).isFalse();
        assertThat(tax.getCode()).isEqualTo("TX-01");
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveToFalse() {
        Tax tax = Tax.createNew("TX-01", "PPN", BigDecimal.ONE, null, false, true);
        tax.softDelete();
        assertThat(tax.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("createNew stores calculation mode and defaults to EXCLUSIVE")
    void createNew_storesCalculationModeAndDefaultsExclusive() {
        Tax explicit = Tax.createNew(
                "TX-IN", "PPN Inclusive", BigDecimal.valueOf(11), "Gross tax", false, true,
                TaxCalculationMode.INCLUSIVE
        );
        Tax fallback = Tax.createNew(
                "TX-EX", "PPN Exclusive", BigDecimal.valueOf(11), "Net tax", false, true,
                null
        );

        assertThat(explicit.getCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
        assertThat(fallback.getCalculationMode()).isEqualTo(TaxCalculationMode.EXCLUSIVE);
    }

    @Test
    @DisplayName("update changes calculation mode without changing code")
    void update_changesCalculationMode() {
        Tax tax = Tax.createNew(
                "TX-01", "PPN", BigDecimal.ONE, "Old Note", false, true,
                TaxCalculationMode.EXCLUSIVE
        );

        tax.update("PPN Inclusive", BigDecimal.TEN, "New Note", true, false, TaxCalculationMode.INCLUSIVE);

        assertThat(tax.getName()).isEqualTo("PPN Inclusive");
        assertThat(tax.getRate()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(tax.getNote()).isEqualTo("New Note");
        assertThat(tax.getIsSubtract()).isTrue();
        assertThat(tax.getIsActive()).isFalse();
        assertThat(tax.getCode()).isEqualTo("TX-01");
        assertThat(tax.getCalculationMode()).isEqualTo(TaxCalculationMode.INCLUSIVE);
    }
}
