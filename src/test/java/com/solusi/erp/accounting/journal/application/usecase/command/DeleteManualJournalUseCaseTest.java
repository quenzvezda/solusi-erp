package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.exception.DomainException;
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
class DeleteManualJournalUseCaseTest {

    @Mock private JournalEntryRepository repository;
    private DeleteManualJournalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteManualJournalUseCaseImpl(repository);
    }

    @Test
    void deleteDraft_delegatesToRepository() {
        when(repository.findById(1L)).thenReturn(Optional.of(UpdateManualJournalUseCaseTest.manualDraft(1L)));

        useCase.execute(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_rejectsPostedAutoAndMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(1L)).isInstanceOf(DomainException.class);

        JournalEntry posted = UpdateManualJournalUseCaseTest.manualDraft(2L);
        posted.post();
        when(repository.findById(2L)).thenReturn(Optional.of(posted));
        assertThatThrownBy(() -> useCase.execute(2L)).isInstanceOf(DomainException.class);

        JournalEntry auto = JournalEntry.createPosted(SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 1L, "GR-001",
                LocalDate.of(2026, 5, 31), "Auto", List.of(
                JournalLine.debit(101L, BigDecimal.TEN),
                JournalLine.credit(201L, BigDecimal.TEN)
        ));
        when(repository.findById(3L)).thenReturn(Optional.of(auto));
        assertThatThrownBy(() -> useCase.execute(3L)).isInstanceOf(DomainException.class);

        verify(repository, never()).deleteById(2L);
        verify(repository, never()).deleteById(3L);
    }
}
