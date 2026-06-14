package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.period.application.usecase.query.EnsureOpenPeriodForDateUseCase;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReversePostedJournalUseCaseTest {

    @Mock private JournalEntryRepository repository;
    @Mock private EnsureOpenPeriodForDateUseCase ensureOpenPeriodForDateUseCase;

    private ReversePostedJournalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReversePostedJournalUseCaseImpl(repository, ensureOpenPeriodForDateUseCase);
    }

    @Test
    void reverse_autoJournalCreatesLinkedPostedReversal() {
        JournalEntry original = autoPosted(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(original));
        when(repository.existsReversalOf(10L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JournalEntry result = useCase.execute(new ReversePostedJournalCommand(
                10L,
                LocalDate.of(2026, 6, 1),
                "Reverse GI"
        ));

        assertThat(result.getStatus()).isEqualTo(JournalStatus.POSTED);
        assertThat(result.getEventType()).isEqualTo(SchemaEventType.GOODS_RECEIPT.name());
        assertThat(result.getSourceType()).isEqualTo("GOODS_RECEIPT");
        assertThat(result.getSourceId()).isNull();
        assertThat(result.getReversalOfId()).isEqualTo(10L);
        assertThat(result.getDescription()).isEqualTo("Reverse GI");

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLines().getFirst().creditAmount()).isEqualByComparingTo("10.0000");
        assertThat(captor.getValue().getLines().get(1).debitAmount()).isEqualByComparingTo("10.0000");
    }

    @Test
    void reverse_manualJournalStillUsesManualIdentity() {
        JournalEntry original = UpdateManualJournalUseCaseTest.manualDraft(11L);
        original.post();
        when(repository.findById(11L)).thenReturn(Optional.of(original));
        when(repository.existsReversalOf(11L)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        JournalEntry result = useCase.execute(new ReversePostedJournalCommand(
                11L,
                LocalDate.of(2026, 6, 1),
                null
        ));

        assertThat(result.getEventType()).isEqualTo(JournalEntry.MANUAL_EVENT_TYPE);
        assertThat(result.getSourceType()).isEqualTo(JournalEntry.MANUAL_SOURCE_TYPE);
        assertThat(result.getSourceId()).isNull();
        assertThat(result.getDescription()).isEqualTo("Reversal of JNL-000011");
    }

    @Test
    void reverse_rejectsNullCommandOrDateAndMissingOriginal() {
        assertThatThrownBy(() -> useCase.execute(null))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.reversal.date.required");
        assertThatThrownBy(() -> useCase.execute(new ReversePostedJournalCommand(1L, null, "Reverse")))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.reversal.date.required");

        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(new ReversePostedJournalCommand(
                1L,
                LocalDate.of(2026, 6, 1),
                "Reverse"
        ))).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.not.found");
    }

    @Test
    void reverse_rejectsDraftReversalChainAlreadyReversedAndClosedPeriod() {
        JournalEntry draft = new JournalEntry(
                new AuditMetadata(1L, 1L, null, null, null, null),
                "MANUAL",
                "MANUAL",
                null,
                null,
                1L,
                BigDecimal.ONE,
                null,
                null,
                LocalDate.of(2026, 5, 31),
                "Draft",
                JournalStatus.DRAFT,
                List.of(
                        JournalLine.manualDebit(101L, BigDecimal.TEN, 1L, BigDecimal.ONE, null),
                        JournalLine.manualCredit(201L, BigDecimal.TEN, 1L, BigDecimal.ONE, null)
                )
        );
        when(repository.findById(1L)).thenReturn(Optional.of(draft));
        assertThatThrownBy(() -> reverse(1L)).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.posted.required");

        JournalEntry postedForReversal = UpdateManualJournalUseCaseTest.manualDraft(2L);
        postedForReversal.post();
        JournalEntry reversal = postedForReversal.createReversal(LocalDate.of(2026, 6, 1), "Reverse");
        when(repository.findById(2L)).thenReturn(Optional.of(reversal));
        assertThatThrownBy(() -> reverse(2L)).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.reversal.chain.not.allowed");

        JournalEntry already = autoPosted(3L);
        when(repository.findById(3L)).thenReturn(Optional.of(already));
        when(repository.existsReversalOf(3L)).thenReturn(true);
        assertThatThrownBy(() -> reverse(3L)).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.already.reversed");

        JournalEntry closed = autoPosted(4L);
        when(repository.findById(4L)).thenReturn(Optional.of(closed));
        when(repository.existsReversalOf(4L)).thenReturn(false);
        doThrow(new DomainException("msg.error.period.not.open"))
                .when(ensureOpenPeriodForDateUseCase).execute(LocalDate.of(2026, 6, 1));
        assertThatThrownBy(() -> reverse(4L)).isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.period.not.open");

        verify(repository, never()).save(any());
    }

    @Test
    void reverse_translatesUniqueConstraintViolation() {
        JournalEntry original = autoPosted(10L);
        when(repository.findById(10L)).thenReturn(Optional.of(original));
        when(repository.existsReversalOf(10L)).thenReturn(false);
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> reverse(10L))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.already.reversed");
    }

    private void reverse(Long id) {
        useCase.execute(new ReversePostedJournalCommand(id, LocalDate.of(2026, 6, 1), "Reverse"));
    }

    private static JournalEntry autoPosted(Long id) {
        return new JournalEntry(
                new AuditMetadata(id, 1L, null, null, null, null),
                SchemaEventType.GOODS_RECEIPT.name(),
                "GOODS_RECEIPT",
                id,
                "GR-%03d".formatted(id),
                null,
                null,
                null,
                null,
                LocalDate.of(2026, 5, 31),
                "Auto",
                JournalStatus.POSTED,
                List.of(
                        JournalLine.debit(101L, new BigDecimal("10.0000")),
                        JournalLine.credit(201L, new BigDecimal("10.0000"))
                )
        );
    }
}
