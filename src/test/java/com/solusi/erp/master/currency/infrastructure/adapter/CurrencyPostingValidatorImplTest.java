package com.solusi.erp.master.currency.infrastructure.adapter;

import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator.CurrencyPostingInfo;
import com.solusi.erp.master.currency.infrastructure.persistence.Currency;
import com.solusi.erp.master.currency.infrastructure.persistence.CurrencyJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrencyPostingValidatorImplTest {

    @Mock
    private CurrencyJpaRepository currencyJpaRepository;

    @InjectMocks
    private CurrencyPostingValidatorImpl validator;

    @Test
    void getPostingInfo_returnsActiveDefaultInfo() {
        when(currencyJpaRepository.findById(1L)).thenReturn(Optional.of(currency(true, true)));

        CurrencyPostingInfo result = validator.getPostingInfo(1L);

        assertThat(result).isNotNull();
        assertThat(result.active()).isTrue();
        assertThat(result.defaultCurrency()).isTrue();
    }

    @Test
    void getPostingInfo_returnsActiveNonDefaultInfo() {
        when(currencyJpaRepository.findById(2L)).thenReturn(Optional.of(currency(true, false)));

        CurrencyPostingInfo result = validator.getPostingInfo(2L);

        assertThat(result).isNotNull();
        assertThat(result.active()).isTrue();
        assertThat(result.defaultCurrency()).isFalse();
    }

    @Test
    void getPostingInfo_returnsInactiveInfo() {
        when(currencyJpaRepository.findById(3L)).thenReturn(Optional.of(currency(false, false)));

        CurrencyPostingInfo result = validator.getPostingInfo(3L);

        assertThat(result).isNotNull();
        assertThat(result.active()).isFalse();
        assertThat(result.defaultCurrency()).isFalse();
    }

    @Test
    void getPostingInfo_returnsNullForMissingCurrency() {
        when(currencyJpaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(validator.getPostingInfo(99L)).isNull();
    }

    @Test
    void getPostingInfo_returnsNullForNullId() {
        assertThat(validator.getPostingInfo(null)).isNull();

        verifyNoInteractions(currencyJpaRepository);
    }

    private static Currency currency(boolean active, boolean defaultCurrency) {
        Currency currency = new Currency();
        currency.setIsActive(active);
        currency.setIsDefault(defaultCurrency);
        return currency;
    }
}
