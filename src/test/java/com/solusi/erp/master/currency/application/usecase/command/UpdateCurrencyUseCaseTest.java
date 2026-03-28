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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateCurrencyUseCase Tests")
class UpdateCurrencyUseCaseTest {

    @Mock
    private CurrencyRepository repository;

    private UpdateCurrencyUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateCurrencyUseCaseImpl(repository);
    }

    @Test
    @DisplayName("execute updates mutable fields; alias remains unchanged")
    void execute_updatesMutableFields() {
        AuditMetadata metadata = new AuditMetadata(1L, 1L, null, null, null, null);
        Currency existing = new Currency(metadata, "$", "USD", "Old Name", "Old Note", false, true);

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Currency.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Currency result = useCase.execute(1L, "€", "New Name", "New Note", false, false);

        assertThat(result.getSymbol()).isEqualTo("€");
        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getAlias()).isEqualTo("USD");
    }

    @Test
    @DisplayName("execute throws DomainException when currency is not found")
    void execute_throwsDomainException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(DomainException.class,
                () -> useCase.execute(99L, "$", "Name", null, false, true));
    }
}
