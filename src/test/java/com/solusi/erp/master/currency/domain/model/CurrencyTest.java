package com.solusi.erp.master.currency.domain.model;

import com.solusi.erp.core.domain.model.AuditMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Currency Domain Model Tests")
class CurrencyTest {

    @Test
    @DisplayName("createNew sets all fields with empty metadata (id == null)")
    void createNew_setsAllFieldsWithEmptyMetadata() {
        Currency currency = Currency.createNew("$", "USD", "US Dollar", "Note", false, true);

        assertThat(currency.getSymbol()).isEqualTo("$");
        assertThat(currency.getAlias()).isEqualTo("USD");
        assertThat(currency.getName()).isEqualTo("US Dollar");
        assertThat(currency.getNote()).isEqualTo("Note");
        assertThat(currency.getIsDefault()).isFalse();
        assertThat(currency.getIsActive()).isTrue();
        assertThat(currency.getId()).isNull();
    }

    @Test
    @DisplayName("Full constructor preserves all fields including metadata")
    void constructor_preservesAllFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Currency currency = new Currency(metadata, "$", "USD", "US Dollar", "Note", false, true);

        assertThat(currency.getId()).isEqualTo(1L);
        assertThat(currency.getAlias()).isEqualTo("USD");
        assertThat(currency.getMetadata()).isEqualTo(metadata);
    }

    @Test
    @DisplayName("update changes mutable fields; alias remains unchanged")
    void update_changesMutableFields() {
        Currency currency = Currency.createNew("$", "USD", "Old Name", "Old Note", false, true);

        currency.update("€", "New Name", "New Note", true, false);

        assertThat(currency.getSymbol()).isEqualTo("€");
        assertThat(currency.getName()).isEqualTo("New Name");
        assertThat(currency.getNote()).isEqualTo("New Note");
        assertThat(currency.getIsDefault()).isTrue();
        assertThat(currency.getIsActive()).isFalse();
        assertThat(currency.getAlias()).isEqualTo("USD");
    }

    @Test
    @DisplayName("unsetDefault sets isDefault to false")
    void unsetDefault_setsIsDefaultToFalse() {
        Currency currency = Currency.createNew("$", "USD", "Dollar", null, true, true);
        currency.unsetDefault();
        assertThat(currency.getIsDefault()).isFalse();
    }

    @Test
    @DisplayName("softDelete sets isActive to false")
    void softDelete_setsIsActiveToFalse() {
        Currency currency = Currency.createNew("$", "USD", "Dollar", null, false, true);
        currency.softDelete();
        assertThat(currency.getIsActive()).isFalse();
    }
}
