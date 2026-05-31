package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator.CurrencyPostingInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateManualJournalUseCaseTest {

    @Mock private JournalEntryRepository repository;
    @Mock private CoaPostingValidator coaPostingValidator;
    @Mock private CurrencyPostingValidator currencyPostingValidator;
    private CreateManualJournalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateManualJournalUseCaseImpl(repository, coaPostingValidator, currencyPostingValidator);
    }

    @Test
    void create_happyPathSavesDraft() {
        allowCurrency(false);
        allowAccounts();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JournalEntry result = useCase.execute(command());

        assertThat(result.isManual()).isTrue();
        assertThat(result.getStatus().name()).isEqualTo("DRAFT");
        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLines()).hasSize(2);
    }

    @Test
    void create_unbalancedDoesNotSave() {
        allowCurrency(false);
        allowAccounts();

        ManualJournalCommand command = new ManualJournalCommand(LocalDate.of(2026, 5, 31), 1L, BigDecimal.ONE,
                null, "Manual", List.of(
                new ManualJournalLineCommand(101L, new BigDecimal("10.0000"), BigDecimal.ZERO, null),
                new ManualJournalLineCommand(201L, BigDecimal.ZERO, new BigDecimal("9.0000"), null)
        ));

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(DomainException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void create_rejectsTooFewLines() {
        allowCurrency(false);
        when(coaPostingValidator.isPostable(101L)).thenReturn(true);

        ManualJournalCommand command = new ManualJournalCommand(LocalDate.of(2026, 5, 31), 1L, BigDecimal.ONE,
                null, "Manual", List.of(new ManualJournalLineCommand(101L, BigDecimal.TEN, BigDecimal.ZERO, null)));

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(DomainException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void create_rejectsMissingInactiveDefaultRateAndInvalidAccount() {
        when(currencyPostingValidator.getPostingInfo(1L)).thenReturn(null);
        assertThatThrownBy(() -> useCase.execute(command())).isInstanceOf(DomainException.class);

        when(currencyPostingValidator.getPostingInfo(1L)).thenReturn(new CurrencyPostingInfo(false, false));
        assertThatThrownBy(() -> useCase.execute(command())).isInstanceOf(DomainException.class);

        when(currencyPostingValidator.getPostingInfo(1L)).thenReturn(new CurrencyPostingInfo(true, true));
        assertThatThrownBy(() -> useCase.execute(commandWithRate(new BigDecimal("2.000000"))))
                .isInstanceOf(DomainException.class);

        allowCurrency(false);
        when(coaPostingValidator.isPostable(101L)).thenReturn(true);
        when(coaPostingValidator.isPostable(201L)).thenReturn(false);
        assertThatThrownBy(() -> useCase.execute(command())).isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void create_acceptsNonDefaultPositiveRate() {
        allowCurrency(false);
        allowAccounts();
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JournalEntry result = useCase.execute(commandWithRate(new BigDecimal("16000.000000")));

        assertThat(result.getExchangeRate()).isEqualByComparingTo("16000.000000");
    }

    private void allowCurrency(boolean defaultCurrency) {
        when(currencyPostingValidator.getPostingInfo(1L)).thenReturn(new CurrencyPostingInfo(true, defaultCurrency));
    }

    private void allowAccounts() {
        when(coaPostingValidator.isPostable(101L)).thenReturn(true);
        when(coaPostingValidator.isPostable(201L)).thenReturn(true);
    }

    static ManualJournalCommand command() {
        return commandWithRate(BigDecimal.ONE);
    }

    static ManualJournalCommand commandWithRate(BigDecimal rate) {
        return new ManualJournalCommand(LocalDate.of(2026, 5, 31), 1L, rate,
                "REF-001", "Manual", List.of(
                new ManualJournalLineCommand(101L, new BigDecimal("10.0000"), BigDecimal.ZERO, "Debit"),
                new ManualJournalLineCommand(201L, BigDecimal.ZERO, new BigDecimal("10.0000"), "Credit")
        ));
    }
}
