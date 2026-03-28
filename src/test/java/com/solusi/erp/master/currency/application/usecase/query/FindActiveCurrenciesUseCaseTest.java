package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.master.currency.domain.model.Currency;
import com.solusi.erp.master.currency.domain.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FindActiveCurrenciesUseCase Tests")
class FindActiveCurrenciesUseCaseTest {

    @Mock
    private CurrencyRepository repository;

    private FindActiveCurrenciesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindActiveCurrenciesUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute returns active currencies from repository")
    void execute_returnsActiveCurrencies() {
        List<Currency> expected = List.of(
                Currency.createNew("Rp", "IDR", "Rupiah", null, true, true),
                Currency.createNew("$", "USD", "US Dollar", null, false, true)
        );
        when(repository.findByIsActiveTrue()).thenReturn(expected);

        List<Currency> result = useCase.execute();

        assertThat(result).isEqualTo(expected);
    }
}
