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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

    private Currency currency(Long id, String alias, boolean isDefault) {
        return new Currency(new AuditMetadata(id, 1L, null, null, null, null),
                "$", alias, "Name", "Note", isDefault, true);
    }

    @Test
    @DisplayName("execute updates mutable fields; alias remains unchanged")
    void execute_updatesMutableFields() {
        Currency existing = currency(1L, "USD", false);
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

    @Test
    @DisplayName("execute unsets other default currencies when isDefault=true")
    void execute_unsetsOtherDefaults_whenSettingAsDefault() {
        Currency target  = currency(1L, "USD", false);
        Currency otherDefault = currency(2L, "EUR", true);

        when(repository.findById(1L)).thenReturn(Optional.of(target));
        when(repository.findByIsDefaultTrue()).thenReturn(List.of(otherDefault));
        when(repository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, "$", "US Dollar", null, true, true);

        // otherDefault (id=2) should have been unset and saved
        assertThat(otherDefault.getIsDefault()).isFalse();
        verify(repository).save(otherDefault);
    }

    @Test
    @DisplayName("execute does NOT unset itself when it is already the default")
    void execute_doesNotUnsetItself_whenAlreadyDefault() {
        Currency target = currency(1L, "USD", true);

        when(repository.findById(1L)).thenReturn(Optional.of(target));
        when(repository.findByIsDefaultTrue()).thenReturn(List.of(target));
        when(repository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, "$", "US Dollar", null, true, true);

        // target should still be the default (unsetDefault must NOT be called on itself)
        assertThat(target.getIsDefault()).isTrue();
        // save is only called once — for the final update, not for unset
        verify(repository).save(target);
    }

    @Test
    @DisplayName("execute skips findByIsDefaultTrue when isDefault is false")
    void execute_skipsDefaultCheck_whenIsDefaultFalse() {
        Currency existing = currency(1L, "USD", true);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(1L, "$", "US Dollar", null, false, true);

        verify(repository, never()).findByIsDefaultTrue();
    }
}
