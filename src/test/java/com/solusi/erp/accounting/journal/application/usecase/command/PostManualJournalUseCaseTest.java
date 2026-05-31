package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator;
import com.solusi.erp.master.currency.domain.port.CurrencyPostingValidator.CurrencyPostingInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostManualJournalUseCaseTest {

    @Mock private JournalEntryRepository repository;
    @Mock private CoaPostingValidator coaPostingValidator;
    @Mock private CurrencyPostingValidator currencyPostingValidator;
    @Mock private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private PostManualJournalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PostManualJournalUseCaseImpl(repository, coaPostingValidator, currencyPostingValidator, ensureOpenPeriodForDateUseCase);
    }

    @Test
    void post_happyPathGuardsPeriodBeforeSave() {
        JournalEntry draft = UpdateManualJournalUseCaseTest.manualDraft(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(draft));
        allowReferences();

        useCase.execute(1L);

        var inOrder = inOrder(ensureOpenPeriodForDateUseCase, repository);
        inOrder.verify(ensureOpenPeriodForDateUseCase).execute(draft.getJournalDate());
        inOrder.verify(repository).save(draft);
    }

    @Test
    void post_closedPeriodPropagatesAndDoesNotSave() {
        JournalEntry draft = UpdateManualJournalUseCaseTest.manualDraft(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(draft));
        allowReferences();
        doThrow(new DomainException("msg.error.period.not.open"))
                .when(ensureOpenPeriodForDateUseCase).execute(draft.getJournalDate());

        assertThatThrownBy(() -> useCase.execute(1L)).isInstanceOf(DomainException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void post_rejectsPostedAutoMissingAndInvalidReferences() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(1L)).isInstanceOf(DomainException.class);

        JournalEntry posted = UpdateManualJournalUseCaseTest.manualDraft(2L);
        posted.post();
        when(repository.findById(2L)).thenReturn(Optional.of(posted));
        assertThatThrownBy(() -> useCase.execute(2L)).isInstanceOf(DomainException.class);

        when(repository.findById(3L)).thenReturn(Optional.of(autoPosted()));
        assertThatThrownBy(() -> useCase.execute(3L)).isInstanceOf(DomainException.class);

        JournalEntry invalidAccount = UpdateManualJournalUseCaseTest.manualDraft(4L);
        when(repository.findById(4L)).thenReturn(Optional.of(invalidAccount));
        when(currencyPostingValidator.getPostingInfo(1L)).thenReturn(new CurrencyPostingInfo(true, false));
        when(coaPostingValidator.isPostable(101L)).thenReturn(false);
        assertThatThrownBy(() -> useCase.execute(4L)).isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }

    private void allowReferences() {
        when(currencyPostingValidator.getPostingInfo(1L)).thenReturn(new CurrencyPostingInfo(true, false));
        when(coaPostingValidator.isPostable(101L)).thenReturn(true);
        when(coaPostingValidator.isPostable(201L)).thenReturn(true);
    }

    static JournalEntry autoPosted() {
        return JournalEntry.createPosted(SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 1L, "GR-001",
                LocalDate.of(2026, 5, 31), "Auto", List.of(
                        JournalLine.debit(101L, BigDecimal.TEN),
                        JournalLine.credit(201L, BigDecimal.TEN)
                ));
    }
}
