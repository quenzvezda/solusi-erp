package com.solusi.erp.accounting.schema.application.usecase.command;

import com.solusi.erp.accounting.journal.domain.model.JournalPosition;
import com.solusi.erp.accounting.journal.domain.model.JournalVariable;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateSchemaUseCaseTest {

    @Mock
    private SchemaRepository repository;

    private CreateSchemaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateSchemaUseCaseImpl(repository);
    }

    @Test
    void execute_savesSchemaWithLines() {
        when(repository.existsByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT)).thenReturn(false);

        List<AccountingSchemaLine> lines = List.of(
                new AccountingSchemaLine(null, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT)
        );

        AccountingSchema saved = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "desc", true, lines);
        when(repository.save(any(AccountingSchema.class))).thenReturn(saved);

        AccountingSchema result = useCase.execute(
                SchemaEventType.GOODS_RECEIPT, "desc", true, lines);

        assertThat(result.getLines()).hasSize(1);
    }

    @Test
    void execute_throwsWhenActiveExists() {
        when(repository.existsByEventTypeAndIsActiveTrue(SchemaEventType.GOODS_RECEIPT)).thenReturn(true);

        List<AccountingSchemaLine> lines = List.of(
                new AccountingSchemaLine(null, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT)
        );

        assertThatThrownBy(() -> useCase.execute(SchemaEventType.GOODS_RECEIPT, "desc", true, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.common.duplicate");

        verify(repository, never()).save(any());
    }
}