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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateSchemaUseCaseTest {

    @Mock
    private SchemaRepository repository;

    private UpdateSchemaUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateSchemaUseCaseImpl(repository);
    }

    @Test
    void execute_updatesSchemaWithLines() {
        List<AccountingSchemaLine> lines = List.of(
                new AccountingSchemaLine(null, JournalVariable.GR_INVENTORY_AMT, 101L, JournalPosition.DEBIT)
        );
        AccountingSchema existing = AccountingSchema.createNew(
                SchemaEventType.GOODS_RECEIPT, "old", false, lines);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any(AccountingSchema.class))).thenReturn(existing);

        AccountingSchema result = useCase.execute(1L, "new", false, lines);

        assertThat(result.getDescription()).isEqualTo("new");
    }

    @Test
    void execute_throwsWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        List<AccountingSchemaLine> lines = List.of();

        assertThatThrownBy(() -> useCase.execute(1L, "desc", true, lines))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("msg.error.notfound");

        verify(repository, never()).save(any());
    }
}