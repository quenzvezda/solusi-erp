package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReverseManualJournalUseCaseTest {

    @Mock private JournalEntryRepository repository;
    @Mock private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;
    private ReverseManualJournalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReverseManualJournalUseCaseImpl(repository, ensureOpenPeriodForDateUseCase);
    }

    @Test
    void reverse_happyPathCreatesPostedReversal() {
        JournalEntry original = UpdateManualJournalUseCaseTest.manualDraft(1L);
        original.post();
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.existsReversalOf(1L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JournalEntry result = useCase.execute(1L, LocalDate.of(2026, 6, 1));

        assertThat(result.getStatus().name()).isEqualTo("POSTED");
        assertThat(result.getReversalOfId()).isEqualTo(1L);
        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLines().getFirst().creditAmount()).isPositive();
    }

    @Test
    void reverse_rejectsClosedPeriodAutoDraftReversalAlreadyReversedMissingAndNullDate() {
        assertThatThrownBy(() -> useCase.execute(1L, null)).isInstanceOf(DomainException.class);

        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(1L, LocalDate.of(2026, 6, 1))).isInstanceOf(DomainException.class);

        when(repository.findById(2L)).thenReturn(Optional.of(PostManualJournalUseCaseTest.autoPosted()));
        assertThatThrownBy(() -> useCase.execute(2L, LocalDate.of(2026, 6, 1))).isInstanceOf(DomainException.class);

        when(repository.findById(3L)).thenReturn(Optional.of(UpdateManualJournalUseCaseTest.manualDraft(3L)));
        assertThatThrownBy(() -> useCase.execute(3L, LocalDate.of(2026, 6, 1))).isInstanceOf(DomainException.class);

        JournalEntry original = UpdateManualJournalUseCaseTest.manualDraft(4L);
        original.post();
        JournalEntry reversal = original.createReversal(LocalDate.of(2026, 6, 1), "Reverse");
        when(repository.findById(4L)).thenReturn(Optional.of(reversal));
        assertThatThrownBy(() -> useCase.execute(4L, LocalDate.of(2026, 6, 2))).isInstanceOf(DomainException.class);

        JournalEntry already = UpdateManualJournalUseCaseTest.manualDraft(5L);
        already.post();
        when(repository.findById(5L)).thenReturn(Optional.of(already));
        when(repository.existsReversalOf(5L)).thenReturn(true);
        assertThatThrownBy(() -> useCase.execute(5L, LocalDate.of(2026, 6, 1))).isInstanceOf(DomainException.class);

        JournalEntry closed = UpdateManualJournalUseCaseTest.manualDraft(6L);
        closed.post();
        when(repository.findById(6L)).thenReturn(Optional.of(closed));
        when(repository.existsReversalOf(6L)).thenReturn(false);
        doThrow(new DomainException("msg.error.period.not.open"))
                .when(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 1));
        assertThatThrownBy(() -> useCase.execute(6L, LocalDate.of(2026, 6, 1))).isInstanceOf(DomainException.class);
    }

    @Test
    void reverse_translatesUniqueConstraintViolation() {
        JournalEntry original = UpdateManualJournalUseCaseTest.manualDraft(1L);
        original.post();
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.existsReversalOf(1L)).thenReturn(false);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> useCase.execute(1L, LocalDate.of(2026, 6, 1)))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.already.reversed");
    }
}
