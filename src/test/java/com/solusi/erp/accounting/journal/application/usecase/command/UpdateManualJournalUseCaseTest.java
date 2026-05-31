package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.coa.domain.port.CoaPostingValidator;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalLine;
import com.solusi.erp.accounting.journal.domain.model.JournalStatus;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.core.domain.model.AuditMetadata;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateManualJournalUseCaseTest {

    @Mock private JournalEntryRepository repository;
    @Mock private CoaPostingValidator coaPostingValidator;
    @Mock private CurrencyPostingValidator currencyPostingValidator;
    private UpdateManualJournalUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateManualJournalUseCaseImpl(repository, coaPostingValidator, currencyPostingValidator);
    }

    @Test
    void updateDraft_happyPath() {
        allowReferences();
        JournalEntry draft = manualDraft(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(draft));
        when(repository.save(draft)).thenReturn(draft);

        JournalEntry result = useCase.execute(1L, CreateManualJournalUseCaseTest.commandWithRate(BigDecimal.ONE));

        assertThat(result.getReferenceNo()).isEqualTo("REF-001");
        verify(repository).save(draft);
    }

    @Test
    void update_rejectsPostedAutoAndMissing() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(1L, CreateManualJournalUseCaseTest.command()))
                .isInstanceOf(DomainException.class);

        JournalEntry posted = manualDraft(2L);
        posted.post();
        when(repository.findById(2L)).thenReturn(Optional.of(posted));
        assertThatThrownBy(() -> useCase.execute(2L, CreateManualJournalUseCaseTest.command()))
                .isInstanceOf(DomainException.class);

        when(repository.findById(3L)).thenReturn(Optional.of(autoPosted()));
        assertThatThrownBy(() -> useCase.execute(3L, CreateManualJournalUseCaseTest.command()))
                .isInstanceOf(DomainException.class);

        verify(repository, never()).save(any());
    }

    private void allowReferences() {
        when(currencyPostingValidator.getPostingInfo(1L)).thenReturn(new CurrencyPostingInfo(true, false));
        when(coaPostingValidator.isPostable(101L)).thenReturn(true);
        when(coaPostingValidator.isPostable(201L)).thenReturn(true);
    }

    static JournalEntry manualDraft(Long id) {
        return new JournalEntry(new AuditMetadata(id, 1L, null, null, null, null), "MANUAL", "MANUAL",
                null, null, 1L, BigDecimal.ONE, null, null, LocalDate.of(2026, 5, 31),
                "Manual", JournalStatus.DRAFT, List.of(
                JournalLine.manualDebit(101L, BigDecimal.TEN, 1L, BigDecimal.ONE, null),
                JournalLine.manualCredit(201L, BigDecimal.TEN, 1L, BigDecimal.ONE, null)
        ));
    }

    private static JournalEntry autoPosted() {
        return JournalEntry.createPosted(SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 1L, "GR-001",
                LocalDate.of(2026, 5, 31), "Auto", List.of(
                JournalLine.debit(101L, BigDecimal.TEN),
                JournalLine.credit(201L, BigDecimal.TEN)
        ));
    }
}
