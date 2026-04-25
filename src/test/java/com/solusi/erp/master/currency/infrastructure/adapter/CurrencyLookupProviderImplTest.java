package com.solusi.erp.master.currency.infrastructure.adapter;

import com.solusi.erp.core.dto.LookupDto;
import com.solusi.erp.master.currency.infrastructure.persistence.Currency;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyLookupProviderImplTest {

    @Mock
    private CurrencyJpaRepository currencyJpaRepository;

    @InjectMocks
    private CurrencyLookupProviderImpl provider;

    @Test
    @DisplayName("resolve() returns LookupDto with alias in payload")
    void resolve_includesAliasInPayload() {
        Currency currency = new Currency();
        currency.setId(1L);
        currency.setName("US Dollar");
        currency.setSymbol("$");
        currency.setAlias("USD");

        when(currencyJpaRepository.findById(1L)).thenReturn(Optional.of(currency));

        LookupDto result = provider.resolve(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("US Dollar");
        assertThat(result.subText()).isEqualTo("$ - USD");
        assertThat(result.payload()).isNotNull();
        assertThat(result.payload().get("symbol")).isEqualTo("$");
        assertThat(result.payload().get("alias")).isEqualTo("USD");
    }

    @Test
    @DisplayName("resolve() preserves existing subText format")
    void resolve_preservesSubTextFormat() {
        Currency currency = new Currency();
        currency.setId(2L);
        currency.setName("Euro");
        currency.setSymbol("€");
        currency.setAlias("EUR");

        when(currencyJpaRepository.findById(2L)).thenReturn(Optional.of(currency));

        LookupDto result = provider.resolve(2L);

        assertThat(result.subText()).isEqualTo("€ - EUR");
    }

    @Test
    @DisplayName("resolve() returns null when currency not found")
    void resolve_returnsNullWhenNotFound() {
        when(currencyJpaRepository.findById(999L)).thenReturn(Optional.empty());

        LookupDto result = provider.resolve(999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve() returns null when currencyId is null")
    void resolve_returnsNullWhenIdNull() {
        LookupDto result = provider.resolve(null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("resolve() handles null symbol gracefully")
    void resolve_handlesNullSymbol() {
        Currency currency = new Currency();
        currency.setId(3L);
        currency.setName("Test Currency");
        currency.setSymbol(null);
        currency.setAlias("TST");

        when(currencyJpaRepository.findById(3L)).thenReturn(Optional.of(currency));

        LookupDto result = provider.resolve(3L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(3L);
        assertThat(result.name()).isEqualTo("Test Currency");
        assertThat(result.subText()).isEqualTo("null - TST");
        assertThat(result.payload()).isNotNull();
        assertThat(result.payload().get("symbol")).isNull();
        assertThat(result.payload().get("alias")).isEqualTo("TST");
    }

    @Test
    @DisplayName("resolve() handles null alias gracefully")
    void resolve_handlesNullAlias() {
        Currency currency = new Currency();
        currency.setId(4L);
        currency.setName("Test Currency");
        currency.setSymbol("T$");
        currency.setAlias(null);

        when(currencyJpaRepository.findById(4L)).thenReturn(Optional.of(currency));

        LookupDto result = provider.resolve(4L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(4L);
        assertThat(result.name()).isEqualTo("Test Currency");
        assertThat(result.subText()).isEqualTo("T$ - null");
        assertThat(result.payload()).isNotNull();
        assertThat(result.payload().get("symbol")).isEqualTo("T$");
        assertThat(result.payload().get("alias")).isNull();
    }

    @Test
    @DisplayName("resolve() handles both null symbol and alias gracefully")
    void resolve_handlesBothNulls() {
        Currency currency = new Currency();
        currency.setId(5L);
        currency.setName("Test Currency");
        currency.setSymbol(null);
        currency.setAlias(null);

        when(currencyJpaRepository.findById(5L)).thenReturn(Optional.of(currency));

        LookupDto result = provider.resolve(5L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.name()).isEqualTo("Test Currency");
        assertThat(result.subText()).isEqualTo("null - null");
        assertThat(result.payload()).isNotNull();
        assertThat(result.payload().get("symbol")).isNull();
        assertThat(result.payload().get("alias")).isNull();
    }
}
