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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
}