package com.solusi.erp.accounting.journal.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalEntry;
import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
import com.solusi.erp.accounting.journal.domain.repository.JournalEntryRepository;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchema;
import com.solusi.erp.accounting.schema.domain.model.AccountingSchemaLine;
import com.solusi.erp.accounting.schema.domain.model.SchemaEventType;
import com.solusi.erp.accounting.schema.domain.repository.SchemaRepository;
import com.solusi.erp.core.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostJournalForEventUseCaseTest {

    @Mock private SchemaRepository schemaRepository;
    @Mock private JournalEntryRepository journalRepository;

    private PostJournalForEventUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new PostJournalForEventUseCaseImpl(schemaRepository, journalRepository);
    }

    private JournalPostingCommand command() {
        return new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 6L, "GR-0006",
                LocalDate.now(), "Auto journal",
                Map.of(
                        JournalVariable.GR_INVENTORY_AMT, new BigDecimal("100"),
                        JournalVariable.GR_GRAND_TOTAL, new BigDecimal("100")
                )
        );
    }

    private AccountingSchema schema() {
        return AccountingSchema.createNew(SchemaEventType.GOODS_RECEIPT, "GR", true, List.of(
                new AccountingSchemaLine(null, JournalVariable.GR_INVENTORY_AMT, 1L, JournalPosition.DEBIT),
                new AccountingSchemaLine(null, JournalVariable.GR_GRAND_TOTAL, 2L, JournalPosition.CREDIT)
        ));
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
    void execute_savesJournalWhenSchemaPresent() {
        when(schemaRepository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(schema()));
        when(journalRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(false);

        useCase.execute(command());

        verify(journalRepository).save(any(JournalEntry.class));
    }

    @Test
    void execute_skipsWhenSourceAlreadyPosted() {
        when(journalRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(true);

        useCase.execute(command());

        verifyNoInteractions(schemaRepository);
        verify(journalRepository, never()).save(any());
    }

    @Test
    void execute_skipsZeroValueSchemaLines() {
        AccountingSchema schema = AccountingSchema.createNew(SchemaEventType.GOODS_RECEIPT, "GR", true, List.of(
                new AccountingSchemaLine(null, JournalVariable.GR_INVENTORY_AMT, 1L, JournalPosition.DEBIT),
                new AccountingSchemaLine(null, JournalVariable.GR_TAX_AMT, 3L, JournalPosition.DEBIT),
                new AccountingSchemaLine(null, JournalVariable.GR_GRAND_TOTAL, 2L, JournalPosition.CREDIT)
        ));
        when(schemaRepository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(schema));
        when(journalRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(false);

        useCase.execute(command());

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getLines())
                .extracting("accountId")
                .containsExactly(1L, 2L);
    }

    @Test
    void execute_preservesOriginalCurrencyAmountsOnDebitAndCreditLines() {
        when(schemaRepository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(schema()));
        when(journalRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(false);
        JournalPostingCommand command = new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 6L, "GR-0006",
                LocalDate.now(), "Auto journal",
                Map.of(
                        JournalVariable.GR_INVENTORY_AMT, new BigDecimal("1600000"),
                        JournalVariable.GR_GRAND_TOTAL, new BigDecimal("1600000")
                ),
                99L,
                new BigDecimal("16000"),
                Map.of(
                        JournalVariable.GR_INVENTORY_AMT, new BigDecimal("100"),
                        JournalVariable.GR_GRAND_TOTAL, new BigDecimal("100")
                )
        );

        useCase.execute(command);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getLines()).hasSize(2);
        assertThat(captor.getValue().getLines().get(0).originalCurrencyId()).isEqualTo(99L);
        assertThat(captor.getValue().getLines().get(0).exchangeRate()).isEqualByComparingTo("16000");
        assertThat(captor.getValue().getLines().get(0).originalDebitAmount()).isEqualByComparingTo("100");
        assertThat(captor.getValue().getLines().get(1).originalCreditAmount()).isEqualByComparingTo("100");
    }

    @Test
    void execute_usesAccountOverrideWhenProvided() {
        when(schemaRepository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(schema()));
        when(journalRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(false);

        JournalPostingCommand command = new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 6L, "GR-0006",
                LocalDate.now(), "Auto journal",
                Map.of(
                        JournalVariable.GR_INVENTORY_AMT, new BigDecimal("100"),
                        JournalVariable.GR_GRAND_TOTAL, new BigDecimal("100")
                ),
                null, null, null,
                Map.of(JournalVariable.GR_GRAND_TOTAL, 999L)
        );

        useCase.execute(command);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getLines()).hasSize(2);
        assertThat(captor.getValue().getLines().get(0).accountId()).isEqualTo(1L);
        assertThat(captor.getValue().getLines().get(1).accountId()).isEqualTo(999L);
    }

    @Test
    void execute_fallsBackToSchemaAccountWhenNoOverride() {
        when(schemaRepository.findByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT))
                .thenReturn(Optional.of(schema()));
        when(journalRepository.existsBySource("GOODS_RECEIPT", 6L)).thenReturn(false);

        JournalPostingCommand command = new JournalPostingCommand(
                SchemaEventType.GOODS_RECEIPT, "GOODS_RECEIPT", 6L, "GR-0006",
                LocalDate.now(), "Auto journal",
                Map.of(
                        JournalVariable.GR_INVENTORY_AMT, new BigDecimal("100"),
                        JournalVariable.GR_GRAND_TOTAL, new BigDecimal("100")
                ),
                null, null, null,
                Map.of()
        );

        useCase.execute(command);

        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(journalRepository).save(captor.capture());
        assertThat(captor.getValue().getLines().get(0).accountId()).isEqualTo(1L);
        assertThat(captor.getValue().getLines().get(1).accountId()).isEqualTo(2L);
    }
}
