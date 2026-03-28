package com.solusi.erp.master.currency.application.usecase.query;

import com.solusi.erp.core.domain.model.Page;
import com.solusi.erp.core.domain.model.Pageable;
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
@DisplayName("FindCurrenciesUseCase Tests")
class FindCurrenciesUseCaseTest {

    @Mock
    private CurrencyRepository repository;

    private FindCurrenciesUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new FindCurrenciesUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute delegates keyword and pageable to repository and returns its result")
    void execute_delegatesKeywordAndPageableToRepository() {
        Pageable pageable = Pageable.of(0, 20);
        Currency currency = Currency.createNew("$", "USD", "Dollar", null, false, true);
        Page<Currency> expectedPage = new Page<>(List.of(currency), 0, 20, 1L);

        when(repository.findAll("usd", pageable)).thenReturn(expectedPage);

        Page<Currency> result = useCase.execute("usd", pageable);

        assertThat(result).isEqualTo(expectedPage);
    }
}
