package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDefaultCurrencyUseCase Tests")
class GetDefaultCurrencyUseCaseTest {

    @Mock
    private CurrencyRepository repository;

    private GetDefaultCurrencyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetDefaultCurrencyUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute returns first default currency when available")
    void execute_returnsFirstDefaultCurrency() {
        Currency defaultCurrency = new Currency(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "Rp", "IDR", "Rupiah", null, true, true);
        when(repository.findByIsDefaultTrue()).thenReturn(List.of(defaultCurrency));

        Optional<Currency> result = useCase.execute();

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("execute returns empty when no default currency exists")
    void execute_returnsEmptyWhenNoDefaultCurrency() {
        when(repository.findByIsDefaultTrue()).thenReturn(List.of());

        Optional<Currency> result = useCase.execute();

        assertThat(result).isEmpty();
    }
}
