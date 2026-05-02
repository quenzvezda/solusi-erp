package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.application.policy.GoodsReceiptJournalPolicy;
import com.solusi.erp.accounting.journal.application.policy.JournalPolicyResolver;
import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostJournalForEventUseCaseTest {

    @Mock private SchemaRepository schemaRepository;
    @Mock private JournalEntryRepository journalEntryRepository;

    private PostJournalForEventUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        JournalPolicyResolver resolver = new JournalPolicyResolver(List.of(new GoodsReceiptJournalPolicy()));
        useCase = new PostJournalForEventUseCaseImpl(schemaRepository, journalEntryRepository, resolver);
    }

    @Test
    void execute_throwsWhenActiveSchemaMissing() {
        when(schemaRepository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(command()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.journal.schema.notfound");
    }

    @Test
    void execute_savesJournalWhenPolicyAndSchemaPresent() {
        when(schemaRepository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(schema()));
        when(journalEntryRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(false);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenAnswer(i -> i.getArgument(0));

        useCase.execute(command());

        verify(journalEntryRepository).save(argThat(entry ->
                "GOODS_RECEIPT".equals(entry.getSourceType())
                && entry.getSourceId().equals(6L)
                && entry.getLines().size() == 2
        ));
    }

    @Test
    void execute_isIdempotentWhenSourceAlreadyPosted() {
        when(journalEntryRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(true);

        useCase.execute(command());

        verify(journalEntryRepository, never()).save(any());
        verify(schemaRepository, never()).findByEventTypeAndIsActiveTrue(any());
    }

    private JournalPostingCommand command() {
        return new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT,
                "GOODS_RECEIPT",
                6L,
                "GR-0006",
                LocalDate.now(),
                "Auto journal",
                new BigDecimal("500.0000"),
                BigDecimal.ZERO,
                new BigDecimal("500.0000")
        );
    }

    private AccountingSchema schema() {
        return AccountingSchema.createNew(SchemaEventType.GOODS_RECEIPT, "GR schema",
                101L, 201L, null, true);
    }
}
