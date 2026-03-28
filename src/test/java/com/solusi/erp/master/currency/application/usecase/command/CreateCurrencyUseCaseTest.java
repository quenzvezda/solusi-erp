package com.solusi.erp.master.currency.application.usecase.command;

import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCurrencyUseCase Tests")
class CreateCurrencyUseCaseTest {

    @Mock
    private CurrencyRepository repository;

    private CreateCurrencyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateCurrencyUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute saves and returns currency when alias is unique")
    void execute_savesCurrencyWhenAliasIsUnique() {
        when(repository.existsByAlias("USD")).thenReturn(false);
        when(repository.save(any(Currency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Currency result = useCase.execute("$", "USD", "US Dollar", "Note", false, true);

        assertThat(result.getAlias()).isEqualTo("USD");
        assertThat(result.getSymbol()).isEqualTo("$");
    }

    @Test
    @DisplayName("execute throws DomainException when alias already exists")
    void execute_throwsDomainException_whenAliasExists() {
        when(repository.existsByAlias("USD")).thenReturn(true);

        assertThrows(DomainException.class,
                () -> useCase.execute("$", "USD", "US Dollar", null, false, true));
    }

    @Test
    @DisplayName("execute unsets previous defaults when isDefault is true")
    void execute_unsetsPreviousDefaults_whenIsDefaultTrue() {
        AuditMetadata metadata = new AuditMetadata(5L, 1L, null, null, null, null);
        Currency existingDefault = new Currency(metadata, "€", "EUR", "Euro", null, true, true);

        when(repository.existsByAlias("USD")).thenReturn(false);
        when(repository.findByIsDefaultTrue()).thenReturn(List.of(existingDefault));
        when(repository.save(any(Currency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("$", "USD", "US Dollar", null, true, true);

        assertThat(existingDefault.getIsDefault()).isFalse();
        verify(repository).save(existingDefault);
    }
}
